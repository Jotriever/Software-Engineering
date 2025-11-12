// === 4. ISP (Interface Segregation Principle) - 인터페이스 분리 원칙 ===
// 클라이언트는 자신이 사용하지 않는 기능에 의존해선 안 됩니다.
// '결제'와 '알림'이라는 최소 단위의 책임으로 인터페이스를 분리합니다.

// 결제 기능만 정의한 인터페이스
interface PaymentProcessor {
    boolean processPayment(double amount);
}

// 알림 기능만 정의한 인터페이스
interface Notifier {
    void sendNotification(String message);
}

// === 2. OCP (Open/Closed Principle) - 개방-폐쇄 원칙 ===
// 기존 코드를 수정하지 않고(Closed), 새로운 기능을 추가(Open)할 수 있어야 합니다.
// PaymentProcessor 인터페이스를 구현한 새로운 결제 수단을 얼마든지 추가할 수 있습니다.

// OCP 구현체 1: 신용카드 결제
class CreditCardProcessor implements PaymentProcessor {
    @Override
    public boolean processPayment(double amount) {
        System.out.println("[신용카드] " + amount + "원 결제 성공.");
        return true; // 실제로는 API 호출 등이 들어감
    }
}

// OCP 구현체 2: 계좌 이체 결제 (새롭게 추가된 기능)
class BankTransferProcessor implements PaymentProcessor {
    @Override
    public boolean processPayment(double amount) {
        System.out.println("[계좌이체] " + amount + "원 입금 확인.");
        return true;
    }
}

// OCP 구현체 3: SMS 알림
class SmsNotifier implements Notifier {
    @Override
    public void sendNotification(String message) {
        System.out.println("[SMS 발송] " + message);
    }
}

// OCP 구현체 4: 이메일 알림 (새롭게 추가된 기능)
class EmailNotifier implements Notifier {
    @Override
    public void sendNotification(String message) {
        System.out.println("[Email 발송] " + message);
    }
}


// === 3. LSP (Liskov Substitution Principle) - 리스코프 치환 원칙 ===
// 하위 타입은 상위 타입으로 완벽하게 대체될 수 있어야 합니다.
// 여기서는 '할인 정책'을 예로 듭니다.

// 상위 타입 (추상 클래스)
abstract class DiscountPolicy {
    // 하위 클래스가 반드시 구현해야 할 할인 적용 메서드
    public abstract double applyDiscount(double price);
}

// 하위 타입 1: 할인이 없는 정책
class NoDiscount extends DiscountPolicy {
    @Override
    public double applyDiscount(double price) {
        return price; // 가격 그대로 반환
    }
}

// 하위 타입 2: VIP 할인 정책
class VipDiscount extends DiscountPolicy {
    private double discountRate = 0.1; // 10% 할인

    @Override
    public double applyDiscount(double price) {
        return price * (1 - discountRate); // 10% 할인 적용
    }
}


// === 1. SRP (Single Responsibility Principle) - 단일 책임 원칙 ===
// 모든 클래스는 '하나의 책임'만 가져야 합니다.

// SRP 1: '주문 데이터'를 보관하는 책임
class Order {
    private double totalAmount;
    // 생성자, Getter 등...
    public Order(double totalAmount) { this.totalAmount = totalAmount; }
    public double getTotalAmount() { return totalAmount; }
}

// SRP 2: '재고'를 관리하는 책임
class StockManager {
    public void decreaseStock(String item) {
        // 재고 감소 로직...
        System.out.println("[재고 관리] " + item + " 재고 1 감소.");
    }
}


// === 5. DIP (Dependency Inversion Principle) - 의존관계 역전 원칙 ===
// 상위 모듈(OrderService)은 하위 모듈(CreditCardProcessor)에 직접 의존하면 안 됩니다.
// 둘 다 '추상화'(PaymentProcessor 인터페이스)에 의존해야 합니다.

class OrderService {
    // [DIP 적용]: 구체적인 클래스(CreditCardProcessor)가 아닌
    // 추상 인터페이스(PaymentProcessor)에 의존합니다.
    private final PaymentProcessor paymentProcessor;
    private final Notifier notifier;
    
    // [SRP 적용]: 재고 관리 책임을 StockManager에게 위임
    private final StockManager stockManager;

    // [DIP 적용]: 외부에서 의존성을 주입(DI)받습니다.
    public OrderService(PaymentProcessor processor, Notifier notifier, StockManager stockManager) {
        this.paymentProcessor = processor;
        this.notifier = notifier;
        this.stockManager = stockManager;
    }

    // [SRP 적용]: 이 클래스의 유일한 책임은 '주문을 처리하는 것'
    public boolean placeOrder(Order order, DiscountPolicy discountPolicy) {
        
        // [LSP 적용]: 'DiscountPolicy'를 사용.
        // 이것이 NoDiscount인지 VipDiscount인지 몰라도 코드는 동일하게 동작합니다.
        double finalPrice = discountPolicy.applyDiscount(order.getTotalAmount());
        System.out.println("최종 결제 금액: " + finalPrice);

        // [DIP 적용]: 추상화에 의존하여 결제 처리
        boolean paymentSuccess = paymentProcessor.processPayment(finalPrice);

        if (paymentSuccess) {
            // [SRP 위임]: 재고 관리 로직 호출
            stockManager.decreaseStock("주스");
            
            // [DIP 적용]: 추상화에 의존하여 알림 발송
            notifier.sendNotification("주문이 성공적으로 완료되었습니다.");
            return true;
        } else {
            notifier.sendNotification("주문 결제에 실패했습니다.");
            return false;
        }
    }
}

// --- 실행 예시 ---
public class JuiceStoreDemo {
    public static void main(String[] args) {
        // 1. 사용할 구현체(하위 모듈)들을 선택 (OCP)
        //    나중에 BankTransferProcessor로 바꿔도 OrderService 코드는 변경할 필요가 없습니다.
        PaymentProcessor paymentMethod = new CreditCardProcessor(); 
        Notifier notifierMethod = new SmsNotifier();
        StockManager stock = new StockManager();

        // 2. 상위 모듈에 의존성 주입 (DIP)
        OrderService orderService = new OrderService(paymentMethod, notifierMethod, stock);

        // 3. 사용할 할인 정책 선택 (LSP)
        DiscountPolicy vipPolicy = new VipDiscount();
        DiscountPolicy normalPolicy = new NoDiscount();

        // 4. 주문 실행
        System.out.println("--- VIP 회원 주문 ---");
        Order order1 = new Order(10000);
        orderService.placeOrder(order1, vipPolicy); // LSP: vipPolicy를 전달

        System.out.println("\n--- 일반 회원 주문 ---");
        Order order2 = new Order(5000);
        orderService.placeOrder(order2, normalPolicy); // LSP: normalPolicy를 전달
    }
}