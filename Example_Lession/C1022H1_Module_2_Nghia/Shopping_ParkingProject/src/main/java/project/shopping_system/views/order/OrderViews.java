package project.shopping_system.views.order;

import project.shopping_system.models.Order;
import project.shopping_system.models.OrderItems;
import project.shopping_system.models.Product;
import project.shopping_system.services.AccountServices;
import project.shopping_system.services.OrderItemServices;
import project.shopping_system.services.OrderServices;
import project.shopping_system.services.ProductServices;
import project.shopping_system.utils.AppUtils;
import project.shopping_system.utils.DateTimeUtil;
import project.shopping_system.views.Options;

import java.time.Instant;
import java.util.List;
import java.util.Scanner;

public class OrderViews {
    Scanner scanner = new Scanner(System.in);
    private OrderServices orderServices;
    private ProductServices productServices;
    private OrderItemServices orderItemServices;
    private AccountServices accountServices = new AccountServices();
    private OrderItemViews orderItemViews;
    public OrderViews(){
        orderServices = OrderServices.getInstance();
        orderItemServices = OrderItemServices.getInstance();
        productServices = ProductServices.getInstance();
        orderItemViews = new OrderItemViews();
    }
    public void showOrderDetail(Order order,Options options){
        if(options != Options.EDIT || options != Options.ADD || options == Options.FIND){
            System.out.printf("|%-8s| %-16s| %-16s| %-10s| %-16s| %-16s| %-5s| %-20s| %-20s|\n","ID", "Full name","Phone Number",
                    "Address","Email","Grand total","ID employee","At created","At updated");
            System.out.printf("|%-8s| %-16s| %-16s| %-10s| %-16s| %-16s| %-5s| %-20s| %-20s|\n",order.getOrderID(),order.getCustomerFullName(),order.getCustomerPhoneNumber(),
                    order.getCustomerAddress(),order.getCustomerEmail(),order.getGrandTotal(),
                    accountServices.findObject(order.getUserID()).getFullName(),DateTimeUtil.formatIntanceToString(order.getAtCreated()),DateTimeUtil.formatIntanceToString(order.getAtUpdated()));
        }
        if (orderServices.isExistObject(order.getOrderID())){
            orderItemViews.showOrderItemExistList(order.getOrderID());
        }
        if (orderServices.isRemoveObject(order.getOrderID()) && options == Options.STATISTICAL) {
            System.out.printf("|%-8s| %-16s| %-16s| %-10s| %-16s| %-16s| %-5s| %-20s| %-20s|\n","ID", "Full name","Phone Number",
                    "Address","Email","Grand total","ID employee","At created","At updated");
            orderItemViews.showOrderItemRemovedList(order.getOrderID());

        }
        if (options == Options.SHOW || options == Options.FIND || options == Options.EDIT && options != Options.STATISTICAL
                && (orderItemViews.showOrderItemExistList(order.getOrderID()))==false ) {//|| orderItemViews.showOrderItemRemovedList(order.getOrderID()) == false
            System.out.println("1.Th??m s???n ph???m\t\t2.In h??a ????n\t\t3.S???a h??a ????n\t\t0.Quay l???i");
            System.out.print(">Ch???n ch???c n??ng: ");
            int choice = AppUtils.retryParseIntInput();
            switch (choice){
                case 1:
                    orderItemViews.add(order.getOrderID());
                    break;
                case 2:
                    remove(order.getOrderID());
                    break;
                case 3:
                    editByID(order.getOrderID());
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Ch???n sai ch???c n??ng.");
                    break;
            }
        }
    }
    public void showOrderList(List<Order> orderList, Options options){
        int count = 0;
        if (options == Options.SHOW || options == Options.EDIT || options == Options.REMOVE || options == Options.STATISTICAL || options == Options.SORT) {
            System.out.printf("|%-8s| %-16s| %-16s| %-10s| %-16s| %-16s| %-5s| %-20s| %-20s|\n","ID", "Full name","Phone Number",
                    "Address","Email","Grand total","Employee","At created","At updated");
            for (Order order : orderList){
                ++count;
                System.out.printf("|%-8s| %-16s| %-16s| %-10s| %-16s| %-16s| %-5s| %-20s| %-20s|\n",order.getOrderID(),order.getCustomerFullName(),order.getCustomerPhoneNumber(),
                        order.getCustomerAddress(),order.getCustomerEmail(),order.getGrandTotal(),
                        accountServices.findObject(order.getUserID()).getFullName(),DateTimeUtil.formatIntanceToString(order.getAtCreated()),DateTimeUtil.formatIntanceToString(order.getAtUpdated()));

            }
        }
        boolean isContinus = false;
        if (options == Options.SORT || options == Options.SHOW || options == Options.STATISTICAL || options == Options.REMOVE){
            do {
                if (count == 0 && options != Options.STATISTICAL){
                    System.out.println("0.Quay l???i.");
                    System.out.print(">Ch???n ch???c n??ng: ");
                    int choice = AppUtils.retryParseIntInput();
                    switch (choice){
                        case 0:
                            isContinus = false;
                            break;
                        default:
                            isContinus = true;
                            System.out.println("Ch???n sai ch???c n??ng.");
                    }
                }
                if (options != Options.REMOVE && options != Options.STATISTICAL){
                    System.out.println("1.Xem chi ti???t h??a ????n.\t\t\t\t0.Quay l???i.");
                    System.out.print(">Ch???n ch???c n??ng: ");
                    int choice = AppUtils.retryParseIntInput();
                    switch (choice){
                        case 1:
                            findOrder(orderList, options);
                            break;
                        case 0:
                            isContinus = false;
                            break;
                        default:
                            isContinus = true;
                            System.out.println("Ch???n sai ch???c n??ng.");
                    }
                }
            }while (isContinus);
        }
    }
    public void add(long userID){
        long orderID = System.currentTimeMillis() % 100000000;
        String customerFullName = AppUtils.retryFullNameInput();
        String customerPhoneNumber = AppUtils.retryPhonenumberInput();
        String customerAddress = AppUtils.retryStreetAdressInput();
        String customerEmail = AppUtils.retryEmailInput();
        double grandTotal = 0;
        Instant atCreated = Instant.now();
        Instant atUpdated = Instant.now();
        Order order = new Order(userID,orderID,customerFullName,customerPhoneNumber,
                customerAddress,customerEmail,grandTotal,atCreated,atUpdated);
        boolean isRetry = false;
        orderServices.add(order);
        showOrderDetail(order,Options.ADD);
        System.out.println("T???o h??a ????n th??nh c??ng!");
        System.out.println("1.Th??m s???n ph???m\t\t\t\t2.S???a h??a ????n\t\t\t\t0.Quay l???i.");
        boolean isContinus = true;
        do {
            System.out.print(">Ch???n ch???c n??ng: ");
            int choice = AppUtils.retryParseIntInput();
            switch (choice){
                case 1:
                    orderItemViews.add(orderID);
                    break;
                case 2:
                    editByID(orderID);
                    break;
                case 0:
                    isContinus = false;
                    break;
                default:
                    System.out.println("Ch???n sai ch???c n??ng. Ki???m tra l???i.");
                    break;
            }
        }while (isContinus);
    }
    public void remove(){
        boolean isRetry = false;
        showOrderList(OrderServices.findAll(),Options.REMOVE);
        System.out.print("Nh???p m?? ID h??a ????n: ");
        long orderID = AppUtils.retryParseLongInput();
        do {
            if (orderServices.isExistObject(orderID)){
                showOrderDetail(orderServices.findObject(orderID),Options.REMOVE);
                System.out.println("B???n ?????ng ?? in h??a ????n n??y?");
                boolean isAccept = AppUtils.isAcceptMenu();
                if (isAccept){
                    orderServices.remove(orderID);
                    orderItemServices.removeAllOrderItemsInOrder(orderID);
                    System.out.println("???? in h??a ????n th??nh c??ng.");
                }
            }else {
                System.out.println("M?? ID n??y kh??ng t???n t???i. Ki???m tra l???i.");
                isRetry = AppUtils.isRetry(Options.REMOVE);
            }
        }while (isRetry);
    }
    private void remove(long orderID){
        if (orderServices.isExistObject(orderID)){
//            showOrderDetail(orderServices.findObject(orderID),Options.REMOVE);
            System.out.println("B???n ?????ng ?? in h??a ????n n??y?");
            if (AppUtils.isAcceptMenu()){
                orderServices.remove(orderID);
                orderItemServices.removeAllOrderItemsInOrder(orderID);
                System.out.println("???? in h??a ????n th??nh c??ng.");
            }
        }else {
            System.out.println("M?? ID n??y kh??ng t???n t???i. Ki???m tra l???i.");
        }
    }
    private void editMenu(){
        boolean isEmptyOrderItem = false;
        //String customerFullName,
        //                 String customerPhoneNumber, String customerAddress, String customerEmail,
        System.out.println(">Qu???n l?? h??a ????n.");
        System.out.println("1.S???a h??? v?? t??n.");
        System.out.println("2.S???a s??? ??i???n tho???i.");
        System.out.println("3.S???a ?????a ch???.");
        System.out.println("4.S???a email.");
        System.out.println("5.S???a danh m???c s???n ph???m h??a ????n.");
        System.out.println("0.Quay l???i.");
        System.out.print(">Ch???n ch???c n??ng: ");
    }
    public void edit(){
        System.out.println(">S???a th??ng tin h??a ????n.");
        showOrderList(OrderServices.findAll(), Options.EDIT);
//        boolean isRetry = false;
//        int choice ;
            System.out.print("Nh???p m?? ID ????n h??ng: ");
            long orderID = AppUtils.retryParseLongInput();
        if (orderServices.isExistObject(orderID)){
            editMenu();
            Order newOrder = new Order();
            newOrder.setOrderID(orderID);
            int choice = AppUtils.retryParseIntInput();
            switch (choice){
                case 1:
                    System.out.print("Nh???p h??? v?? t??n: ");
                    String customerFullName = AppUtils.retryFullNameInput();
                    newOrder.setCustomerFullName(customerFullName);
                    break;
                case 2:
                    System.out.print("Nh???p s??? ??i???n tho???i: ");
                    String customerPhoneNumber = AppUtils.retryPhonenumberInput();
                    newOrder.setCustomerPhoneNumber(customerPhoneNumber);
                    break;
                case 3:
                    System.out.print("Nh???p ?????a ch???: ");
                    String customerAddress = AppUtils.retryStreetAdressInput();
                    newOrder.setCustomerAddress(customerAddress);
                    break;
                case 4:
                    System.out.print("Nh???p email: ");
                    String customerEmail = AppUtils.retryEmailInput();
                    newOrder.setCustomerEmail(customerEmail);
                    break;
                case 5:
                    orderItemViews.edit(orderID);
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Nh???p sai ch???c n??ng. Ki???m tra l???i.");
                    break;
            }
            if ((choice == 1 || choice == 2 || choice == 3 || choice == 4) && choice != 5){
                System.out.println("B???n mu???n c???p nh???p th??ng tin m???i?");
                boolean isAccept = AppUtils.isAcceptMenu();
                if (isAccept){
                    orderServices.edit(newOrder);
                    System.out.println("???? c???p nh???t th??nh c??ng!");
                }
            }
        }else {
            System.out.println("M?? ID n??y kh??ng t???n t???i. Ki???m tra l???i.");
        }
    }
    public void editByID(long orderID){
        if (orderServices.isExistObject(orderID)){
            editMenu();
            Order newOrder = new Order();
            newOrder.setOrderID(orderID);
            int choice = AppUtils.retryParseIntInput();
            switch (choice){
                case 1:
                    System.out.print("Nh???p h??? v?? t??n: ");
                    String customerFullName = AppUtils.retryFullNameInput();
                    newOrder.setCustomerFullName(customerFullName);
                    break;
                    case 2:
                        System.out.print("Nh???p s??? ??i???n tho???i: ");
                        String customerPhoneNumber = AppUtils.retryPhonenumberInput();
                        newOrder.setCustomerPhoneNumber(customerPhoneNumber);
                        break;
                    case 3:
                        System.out.print("Nh???p ?????a ch???: ");
                        String customerAddress = AppUtils.retryStreetAdressInput();
                        newOrder.setCustomerAddress(customerAddress);
                        break;
                    case 4:
                        System.out.print("Nh???p email: ");
                        String customerEmail = AppUtils.retryEmailInput();
                        newOrder.setCustomerEmail(customerEmail);
                        break;
                case 5:
                    orderItemViews.edit(orderID);
                    break;
                    case 0:
                        break;
                    default:
                        System.out.println("Nh???p sai ch???c n??ng. Ki???m tra l???i.");
                        break;
                }
                if (choice!=0){
                    System.out.println("B???n mu???n c???p nh???p th??ng tin m???i?");
                    boolean isAccept = AppUtils.isAcceptMenu();
                    if (isAccept){
                        orderServices.edit(newOrder);
                    }
                }
            }else {
                System.out.println("M?? ID n??y kh??ng t???n t???i. Ki???m tra l???i.");
            }
    }
    public void findOrderAdminMenu(){
        boolean isRetry = false;
        do {
            System.out.println(">T??m ki???m h??a ????n.");
            System.out.println("1.H??a ????n ch??a in.");
            System.out.println("2.H??a ????n ???? in.");
            System.out.println("0.Quay l???i.");
            System.out.print(">Ch???n ch???c n??ng: ");
            int choice = AppUtils.retryParseIntInput();
            switch (choice){
                case 1:
                    findExistOrder();
                    break;
                case 2:
                    findRemovedOrder();
                    break;
                case 0:
                    isRetry = false;
                    break;
                default:
                    isRetry = true;
                    System.out.println("Ch???n sai ch???c n??ng. Ki???m tra l???i.");
                    break;
            }
            if (choice != 1 && choice != 2 && choice !=0)
                isRetry = AppUtils.isRetry(Options.FIND);
        }while (isRetry);
    }
    public void findOrder(List<Order> orderList, Options options){
        showOrderList(orderList, Options.FIND);
        boolean isContinus = false;
        do {
            System.out.print("Nh???p m?? ID h??a ????n: ");
            long orderID = AppUtils.retryParseLongInput();
            if (orderServices.isExistObject(orderID)){
                Order order = orderServices.findObject(orderID);
                showOrderDetail(order,Options.SHOW);
//                showOrderDetail(orderServices.findObject(orderID),Options.SHOW);
            } else if (orderServices.isRemoveObject(orderID) && options == Options.STATISTICAL) {
                orderItemViews.showOrderItemRemovedList(orderID);
                System.out.println("Nh???p ph??m b???t k?? ????? ti???p t???c!");
                String anyKey = scanner.nextLine();
                //Order order = orderServices.findRemovedObject(orderID);
                //showOrderDetail(order,Options.STATISTICAL);
//                showOrderDetail(orderServices.findRemovedObject(orderID),Options.STATISTICAL);
            } else{
                System.out.println("M?? ID h??a ????n n??y kh??ng t???n t???i. Ki???m tra l???i.");
                isContinus = AppUtils.isRetry(Options.FIND);
            }
        }while (isContinus);

    }
    public void findExistOrder(){
        showOrderList(OrderServices.findAll(),Options.STATISTICAL);
        System.out.print("Nh???p m?? ID h??a ????n: ");
        long orderID = AppUtils.retryParseLongInput();
        if (orderServices.isExistObject(orderID)){
            showOrderDetail(orderServices.findObject(orderID),Options.SHOW);
        }else
            System.out.println("M?? ID h??a ????n n??y kh??ng t???n t???i. Ki???m tra l???i.");
    }
    private void findRemovedOrder(){
        showOrderList(OrderServices.findAllOrdersRemoved(),Options.REMOVE);
        System.out.print("Nh???p m?? ID h??a ????n: ");
        long orderID = AppUtils.retryParseLongInput();
        if (orderServices.isRemoveObject(orderID)){
            showOrderDetail(orderServices.findRemovedObject(orderID),Options.STATISTICAL);
            //xem chi ti???t h??a ????n, orderItemViews
        }else
            System.out.println("M?? ID h??a ????n n??y kh??ng t???n t???i. Ki???m tra l???i.");
    }
    public void sortOrderMenuAdmin(){
        int choice;
        boolean isRetry = true;
        do {
            System.out.println(">S???p x???p h??a ????n.");
            System.out.println("1.H??a ????n ch??a in.");
            System.out.println("2.H??a ????n ???? in.");
            System.out.println("0.Quay l???i.");
            System.out.print(">Ch???n ch???c n??ng: ");
            choice = AppUtils.retryParseIntInput();
            switch (choice){
                case 1:
                    sortExistOrderMenu();
                    isRetry = false;
                    break;
                case 2:
                    sortRemovedOrderMenu();
                    isRetry = false;
                    break;
                case 0:
                    isRetry = false;
                    break;
                default:
                    System.out.println("Ch???n sai ch???c n??ng. Ki???m tra l???i.");
                    break;
            }
        }while (isRetry);

    }
    public void sortExistOrderMenu(){
        boolean isRetry = true;
        int choice;
        do {
            System.out.println(">S???p x???p h??a ????n.");
            System.out.println("1.Theo t???ng ti???n h??a ????n.");
            System.out.println("2.Theo h??? t??n kh??ch h??ng.");
            System.out.println("0.Quay l???i.");
            System.out.print(">Ch???n ch???c n??ng: ");
            choice = AppUtils.retryParseIntInput();
            switch (choice){
                case 1:
                    sortByGrandTotalOrderMenu();
                    isRetry = false;
                    break;
                case 2:
                    sortByCustomerFullNameOrderMenu();
                    isRetry = false;
                    break;
                case 0:
                    isRetry = false;
                    break;
                default:
                    System.out.println("Ch???n sai ch???c n??ng. Ki???m tra l???i.");
                    System.out.print("Nh???p l???i: ");
                    break;
            }
        }while (isRetry);
    }
    private void sortRemovedOrderMenu(){
        boolean isRetry = false;
        int choice;
        do {
            System.out.println(">Menu s???p x???p h??a ????n.");
            System.out.println("1.Theo t???ng ti???n h??a ????n.");
            System.out.println("2.Theo h??? t??n kh??ch h??ng.");
            System.out.println("0.Quay l???i.");
            System.out.print(">Ch???n ch???c n??ng: ");
            choice = AppUtils.retryParseIntInput();
            switch (choice){
                case 1:
                    sortByGrandTotalOrderRemovedMenu();
                    isRetry = false;
                    break;
                case 2:
                    sortByCustomerFullNameOrderRemoveMenu();
                    isRetry = false;
                    break;
                case 0:
                    isRetry = false;
                    break;
                default:
                    System.out.println("Ch???n sai ch???c n??ng. Ki???m tra l???i.");
                    isRetry = true;
                    break;
            }
        }while (isRetry);
    }
    private void sortByGrandTotalOrderRemovedMenu(){
        boolean isRetry = false;
        int choice;
        do {
            System.out.println(">Menu s???p x???p h??a ????n.");
            System.out.println("1.Theo t???ng ti???n t??ng d???n.");
            System.out.println("2.Theo t???ng ti???n gi???m d???n.");
            System.out.println("0.Quay l???i.");
            System.out.print(">Ch???n ch???c n??ng: ");
            choice = AppUtils.retryParseIntInput();
            switch (choice){
                case 1:
                    showOrderList(orderServices.sortByGrandTotalIncreaseOrderRemoved(),Options.STATISTICAL);
                    isRetry = false;
                    break;
                case 2:
                    showOrderList(orderServices.sortByGrandTotalDecreaseOrderRemoved(),Options.STATISTICAL);
                    isRetry = false;
                    break;
                case 0:
                    isRetry = false;
                    break;
                default:
                    System.out.println(">Ch???n sai ch???c n??ng. Ki???m tra la???.");
                    System.out.print(">Nh???p l???i: ");
                    isRetry = true;
                    break;
            }
        }while (isRetry);
    }
    private void sortByCustomerFullNameOrderRemoveMenu(){
        boolean isRetry = true;
        int choice;
        do {
            System.out.println(">Theo h??? t??n kh??ch h??ng.");
            System.out.println("1.S???p x???p t??? A ?????n Z.");
            System.out.println("2.S???p x???p t??? Z ?????n A.");
            System.out.println("0.Quay l???i.");
            System.out.print(">Ch???n ch???c n??ng: ");
            choice = AppUtils.retryParseIntInput();
            switch (choice){
                case 1:
                    showOrderList(orderServices.sortByNameAToZOrderRemoved(),Options.STATISTICAL);
                    isRetry = false;
                    break;
                case 2:
                    showOrderList(orderServices.sortByNameZToAOrderRemoved(),Options.STATISTICAL);
                    isRetry = false;
                    break;
                case 0:
                    isRetry = false;
                    break;
                default:
                    System.out.println("Ch???n sai ch???c n??ng. Ki???m tra l???i.");
                    break;
            }
        }while (isRetry);
    }
    private void sortByCustomerFullNameOrderMenu(){
        boolean isRetry = true;
        int choice;
        do {
            System.out.println(">Theo h??? t??n kh??ch h??ng.");
            System.out.println("1.S???p x???p t??? A ?????n Z.");
            System.out.println("2.S???p x???p t??? Z ?????n A.");
            System.out.println("0.Quay l???i.");
            System.out.print(">Ch???n ch???c n??ng: ");
            choice = AppUtils.retryParseIntInput();
            switch (choice){
                case 1:
                    showOrderList(orderServices.sortByNameAToZ(),Options.SORT);
                    isRetry = false;
                    break;
                case 2:
                    showOrderList(orderServices.sortByNameZToA(),Options.SORT);
                    isRetry = false;
                    break;
                case 0:
                    isRetry = false;
                    break;
                default:
                    System.out.println("Ch???n sai ch???c n??ng. Ki???m tra l???i.");
                    System.out.print("Nh???p l???i: ");
                    break;
            }
        }while (isRetry);
    }
    private void sortByGrandTotalOrderMenu(){
        boolean isRetry = false;
        int choice;
            System.out.println(">Theo t???ng ti???n h??a ????n");
            System.out.println("1.S???p x???p theo t???ng ti???n t??ng d???n.");
            System.out.println("2.S???p x???p theo t???ng ti???n gi???m d???n.");
            System.out.println("0.Quay l???i.");
            System.out.print(">Ch???n ch???c n??ng: ");
        do {
            choice = AppUtils.retryParseIntInput();
            switch (choice){
                case 1:
                    showOrderList(orderServices.sortByGrandTotalIncrease(),Options.SORT);
                    isRetry = false;
                    break;
                case 2:
                    showOrderList(orderServices.sortByGrandTotalDecrease(),Options.SORT);
                    isRetry = false;
                    break;
                case 0:
                    isRetry = false;
                    break;
                default:
                    System.out.println("Ch???n sai ch???c n??ng. Ki???m tra la???.");
                    System.out.print(">Nh???p l???i: ");
                    isRetry = true;
                    break;
            }
        }while (isRetry);
    }
    public void statisticalByDay(){
        Instant days = AppUtils.retryDayInput();
        System.out.printf("??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????? DOANH THU NG??Y %s ????????????????????????????????????????????????????????????????????????????????????????????????????????????\n", DateTimeUtil.formatDayIntanceToString(days));
        double statisticalByDay = orderServices.statisticalByDay(days);
        showStatistical(statisticalByDay, orderServices.statisticalByDayList(days));
    }
    public void statisticalByMonth(){
        Instant months = AppUtils.retryMonthInput();
        System.out.printf("??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????? DOANH THU TH??NG %s ????????????????????????????????????????????????????????????????????????????????????????????????????????????\n", DateTimeUtil.formatMonthIntanceToString(months));
        double statisticalByMonth = orderServices.statisticalByMonth(months);
        showStatistical(statisticalByMonth,orderServices.statisticalByYearList(months));
    }
    public void statisticalByYear(){
        Instant years = AppUtils.retryYearInput();
        System.out.printf("??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????? DOANH THU N??M %s ????????????????????????????????????????????????????????????????????????????????????????????????????????????\n", DateTimeUtil.formatYearIntanceToString(years));
        double statisticalByYear = orderServices.statisticalByYear(years);
        showStatistical(statisticalByYear,orderServices.statisticalByYearList(years));
    }
    private void showStatistical(double statistical, List<Order> orderList){
        showOrderList(orderList,Options.STATISTICAL);
        System.out.println("???-----------------------------------------------------------------------------------------------------???");
        System.out.printf("???                                                         T???NG DOANH THU: %-20s  ???\n", statistical);
        System.out.println("???-----------------------------------------------------------------------------------------------------???");
        System.out.println("Nh???p ph??m b???t k?? ????? ti???p t???c.");
        String anyKey = scanner.nextLine();
    }
}

