import java.util.ArrayList;
import java.util.List;

// === 3. LSP (리스코프 치환 원칙) & 5. DIP (의존관계 역전 원칙) ===
// [DIP] 상위 모듈(JuiceMaker)이 구체적인 '딸기'가 아닌 'Fruit' 추상화에 의존합니다.
// [LSP] Fruit를 상속받은 모든 하위 클래스(딸기, 바나나)는 Fruit 타입으로 완벽히 대체될 수 있습니다.
abstract class Fruit {
    protected String name;
    protected int pricePer100g;

    public Fruit(String name, int pricePer100g) {
        this.name = name;
        this.pricePer100g = pricePer100g;
    }

    public String getName() { return name; }
    public int getPricePer100g() { return pricePer100g; }

    // [SRP] 과일 클래스는 자신의 속성(이름, 가격)과 간단한 행위(갈기)만 담당합니다.
    public abstract String blend();
}

// === 2. OCP (개방-폐쇄 원칙) ===
// 새로운 과일(예: 망고)이 추가되어도(확장에는 Open), 
// 기존 JuiceMaker 클래스의 코드는 수정할 필요가 없습니다(수정에는 Closed).

// OCP 확장 1
class Strawberry extends Fruit {
    public Strawberry() { super("딸기", 1000); }
    @Override public String blend() { return "새콤한 딸기"; }
}

// OCP 확장 2
class Banana extends Fruit {
    public Banana() { super("바나나", 700); }
    @Override public String blend() { return "부드러운 바나나"; }
}

// OCP 확장 3 (새로 추가된 과일)
class Mango extends Fruit {
    public Mango() { super("망고", 1500); }
    @Override public String blend() { return "달콤한 망고"; }
}


// === 1. SRP (단일 책임 원칙) ===
// 'JuiceMaker' 클래스는 '주스를 조합하고 가격을 계산하는 로직'이라는 단일 책임만 가집니다.
// (과일 데이터 관리 책임은 Fruit 클래스가 가집니다)
class JuiceMaker {
    
    // [DIP] JuiceMaker는 구체적인 과일이 아닌 Fruit 추상화에 의존합니다.
    public double calculatePrice(List<Fruit> fruits) {
        double totalPrice = 0;
        
        // [LSP] 'fruit'가 Strawberry든 Banana든 상관없이 동일하게 동작합니다.
        for (Fruit fruit : fruits) {
            totalPrice += fruit.getPricePer100g(); // 100g 단위로 계산 가정
        }
        return totalPrice;
    }

    public String blendJuice(List<Fruit> fruits) {
        String result = "";
        for (Fruit fruit : fruits) {
            result += fruit.blend() + " "; // 각 과일의 blend() 호출
        }
        return "주스 완성: " + result;
    }
}


// === 4. ISP (인터페이스 분리 원칙) ===
// 클라이언트(고객, 관리자)의 역할에 맞게 인터페이스를 분리합니다.

// ISP 분리 1: 고객용 기능
interface ICustomerActions {
    void orderJuice(List<Fruit> fruits);
    void checkPrice(List<Fruit> fruits);
}

// ISP 분리 2: 관리자용 기능
interface IAdminActions {
    void addFruitToStock(Fruit fruit);
    // 고객은 이 기능을 알 필요가 없습니다.
}


// --- 실행 ---
// ISP 구현: 고객 클래스
class Customer implements ICustomerActions {
    private JuiceMaker maker = new JuiceMaker(); // 편의상 내부 생성

    @Override
    public void orderJuice(List<Fruit> fruits) {
        System.out.println(maker.blendJuice(fruits));
        System.out.println("가격: " + maker.calculatePrice(fruits) + "원");
    }

    @Override
    public void checkPrice(List<Fruit> fruits) {
        System.out.println("예상 가격: " + maker.calculatePrice(fruits) + "원");
    }
}

// ISP 구현: 관리자 클래스
class Admin implements IAdminActions {
    @Override
    public void addFruitToStock(Fruit fruit) {
        // [LSP] fruit가 딸기든 망고든 상관없이 이름만 가져와서 처리
        System.out.println("[관리자] " + fruit.getName() + " 재고 추가 완료.");
    }
}


// --- 메인 클래스 ---
public class JuiceStoreSolidDemo {
    public static void main(String[] args) {
        // 1. 고객이 과일을 선택합니다. (LSP: 모든 과일은 Fruit 타입으로 다뤄짐)
        List<Fruit> strawberryBananaJuice = new ArrayList<>();
        strawberryBananaJuice.add(new Strawberry());
        strawberryBananaJuice.add(new Banana());

        // 2. 고객(ICustomerActions)이 주문합니다.
        Customer customer = new Customer();
        customer.orderJuice(strawberryBananaJuice);

        System.out.println("---");

        // 3. [OCP] 새로운 '망고' 과일을 추가해도...
        List<Fruit> mangoBananaJuice = new ArrayList<>();
        mangoBananaJuice.add(new Mango()); // 새로 추가된 과일 사용
        mangoBananaJuice.add(new Banana());
        
        // ... 기존 JuiceMaker나 Customer 코드를 수정할 필요가 없습니다.
        customer.orderJuice(mangoBananaJuice);
        
        System.out.println("---");
        
        // 4. 관리자(IAdminActions)는 자신의 기능만 사용합니다.
        Admin admin = new Admin();
        admin.addFruitToStock(new Mango());
    }
}