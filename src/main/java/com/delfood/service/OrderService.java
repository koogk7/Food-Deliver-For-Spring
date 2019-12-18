package com.delfood.service;

import com.delfood.controller.response.OrderResponse;
import com.delfood.dto.AddressDTO;
import com.delfood.dto.ItemsBillDTO;
import com.delfood.dto.ItemsBillDTO.MenuInfo;
import com.delfood.dto.ItemsBillDTO.ShopInfo;
import com.delfood.dto.ItemsBillDTO.MenuInfo.OptionInfo;
import com.delfood.error.exception.order.TotalPriceMismatchException;
import com.delfood.dto.MemberDTO;
import com.delfood.dto.MenuDTO;
import com.delfood.dto.OptionDTO;
import com.delfood.dto.OrderDTO;
import com.delfood.dto.OrderItemDTO;
import com.delfood.dto.OrderItemOptionDTO;
import com.delfood.dto.OrderBillDTO;
import com.delfood.mapper.OptionMapper;
import com.delfood.mapper.OrderMapper;
import com.delfood.utils.OrderUtil;
import lombok.extern.log4j.Log4j2;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Log4j2
public class OrderService {
  @Autowired
  private OrderMapper orderMapper;

  @Autowired
  private MemberService memberService;
  
  @Autowired
  private MenuService menuService;
  
  @Autowired
  private OptionService optionService;
  
  @Autowired
  private ShopService shopService;
  
  @Autowired
  private AddressService addressService;
  
  /**
   * <b>미완성 로직</b><br>
   * 주문 요청을 진행한다.
   * 사용자가 주문 요청시 전달받은 가격과, 서버에서 직접 비교한 가격을 비교하여 다르면 예외처리 할 예정.
   * @param memberId 고객 아이디
   * @param items 주문한 아이템들
   * @return
   */
  @Transactional
  public OrderResponse order(String memberId, List<OrderItemDTO> items, long totalPriceFromClient) {
    // 클라이언트가 계산한 금액과 서버에서 계산한 금액이 같은지 비교
    long totalPriceFromServer = totalPrice(memberId, items);
    if (totalPriceFromServer != totalPriceFromClient) {
      log.error("Total Price Mismatch! client price : {}, server price : {}",
          totalPriceFromClient,
          totalPriceFromServer);
      throw new TotalPriceMismatchException("Total Price Mismatch!");
    }
    
    // 주문 준비 작업. 결제 전.
    Long orderId = preOrder(memberId, items);
    
    // 결제 진행
    
    
    // 계산서 발행
    ItemsBillDTO bill = getBill(memberId, items);
    
    // 사장님에게 알림(푸시)
    
    
    return new OrderResponse(bill, orderId);
  }
  
  /**
   * 주문 테이블에 insert를 진행한다.
   * 주문 메뉴, 주문 옵션이 추가된다.
   * 
   * @param memberId 고객 아이디
   * @param items 주문할 아이템들
   * @return
   */
  @Transactional
  private Long preOrder(String memberId, List<OrderItemDTO> items) {
    OrderDTO order = new OrderDTO();
    MemberDTO memberInfo = memberService.getMemberInfo(memberId);
    order.setMemberId(memberId);
    order.setAddressCode(memberInfo.getAddressCode());
    order.setAddressDetail(memberInfo.getAddressDetail());

    orderMapper.addOrder(order);
    Long orderId = order.getId();

    log.debug("addOrder Finished");
    log.debug("order id : {}", orderId);

    List<OrderItemOptionDTO> options = new ArrayList<OrderItemOptionDTO>();
    
    for (int i = 0; i < items.size(); i++) {
      OrderItemDTO item = items.get(i);
      item.setId(OrderUtil.generateOrderItemKey(memberId, i));
      item.setOrderId(orderId);
      for (int j = 0; j < item.getOptions().size(); j++) {
        OrderItemOptionDTO option = item.getOptions().get(j);
        option.setId(OrderUtil.generateOrderItemOptionKey(memberId, i, j));
        option.setOrderItemId(item.getId());
        options.add(option);
      }
    }
    
    orderMapper.addOrderItems(items);
    orderMapper.addOrderItemOptions(options);

    return order.getId();
  }
  
  
  /**
   * 고객이 선정한 메뉴의 총 계산서를 출력한다.
   * @author jun
   * @param memberId 고객 아이디
   * @param items 주문 상품들. 메뉴, 옵션 리스트가 존재한다.
   * @return
   */
  @Transactional(readOnly = true)
  public ItemsBillDTO getBill(String memberId, List<OrderItemDTO> items) {
    // 고객 주소 정보 추출
    AddressDTO addressInfo = memberService.getMemberInfo(memberId).getAddressInfo();
    // 매장 정보 추출
    ShopInfo shopInfo = shopService.getShopByMenuId(items.get(0).getMenuId());
    // 배달료 계산
    long deliveryPrice = addressService.deliveryPrice(memberId, shopInfo.getId());
    
    // 계산서 생성
    ItemsBillDTO bill = ItemsBillDTO.builder()
                                    .memberId(memberId)
                                    .addressInfo(addressInfo)
                                    .shopInfo(shopInfo)
                                    .deliveryPrice(deliveryPrice)
                                    .menus(orderMapper.findItemsBill(items))
                                    .build();
    
//    /// 계산서에 각 항목별로 가격과 이름 등을 추가
//    for (OrderItemDTO item : items) {
//      MenuDTO menuInfo = menuService.getMenuInfo(item.getMenuId());
//      MenuInfo billMenuInfo = MenuInfo.builder()
//          .id(menuInfo.getId())
//          .name(menuInfo.getName())
//          .price(menuInfo.getPrice())
//          .build();
//      
//      // 항목별 옵션 정보 추가
//      for (OrderItemOptionDTO orderItemOption : item.getOptions()) {
//        OptionDTO optionInfo = optionService.getOptionInfo(orderItemOption.getOptionId());
//        OptionInfo billOptionInfo = OptionInfo.builder()
//                                              .id(optionInfo.getId())
//                                              .name(optionInfo.getName())
//                                              .price(optionInfo.getPrice())
//                                              .build();
//        billMenuInfo.getOptions().add(billOptionInfo);
//      }
//      bill.getMenus().add(billMenuInfo);
//    }
    return bill;
  }
  
  /**
   * 총 가격을 계산한다.
   * @author jun
   * @param items 계산할 아이템들
   * @return 총 가격
   */
  @Transactional(readOnly = true)
  public long totalPrice(String memberId, List<OrderItemDTO> items) {
    long totalPrice = orderMapper.findItemsPrice(items);
    long deliveryPrice = addressService.deliveryPrice(memberId,
        shopService.getShopByMenuId(items.get(0).getMenuId()).getId());
    
    return totalPrice + deliveryPrice;
  }
  
  
  /**
   * 두 주소 사이 거리(Meter 단위)를 조회한다.
   * @param startAddressCode 시작 주소
   * @param endAddressCode 도착 주소
   * @return
   */
  public double addressDistance(String startAddressCode, String endAddressCode) {
    return addressService.getDistanceMeter(startAddressCode, endAddressCode);
  }
  
  /**
   * 주문 정보를 조회한다.
   * @param orderId 주문 아이디
   * @return
   */
  public OrderBillDTO getPreOrderBill(Long orderId) {
    return orderMapper.findOrderBill(orderId);
  }
  

}
