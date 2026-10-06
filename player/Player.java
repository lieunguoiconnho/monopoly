package ProjectOop.player;

import ProjectOop.square.*;
import java.util.ArrayList;
import java.util.List;

public class Player {

    //1.Thông tin ban đầu
    private String name;       // ✅ Sửa: private (Encapsulation)
    private int balance;
    private int position;      // ✅ Sửa: private (Encapsulation)
    private boolean isBankrupt;
    private int lastDiceRoll;
    private Player creditor; // Chủ nợ gây phá sản (null = ngân hàng)

    //2.Tài sản
    private List<PropertySquare> ownedProperties;
    private int getOutOfJailFreeTickets;

    //3.Đi tù
    private boolean inJail;
    private int turnsInJail;

    public static final int INITIAL_BALANCE = 1500;

    //4.constructor của 1 player
    public Player(String name)
    {
        this(name, INITIAL_BALANCE);
    }

    public Player(String name, int initialBalance)
    {
        this.name = name;
        this.position = 0;
        this.balance = initialBalance;
        this.isBankrupt = false;
        this.lastDiceRoll = 0;
        this.inJail = false;
        this.getOutOfJailFreeTickets = 0;
        this.creditor = null; // ✅ Sửa: khởi tạo tường minh
        this.ownedProperties = new ArrayList<>();
    }

    private boolean passedGoRecently = false;

    public boolean checkAndResetPassedGo() {
        if (passedGoRecently) {
            passedGoRecently = false;
            return true;
        }
        return false;
    }

    //5.di chuyển
    public void move(int step)
    {
        if (inJail) return; // ✅ Sửa: không di chuyển khi đang ở tù
        int newPosition = this.position + step; // không thay đổi position ngay
        if(newPosition >= 40){
            passGo();
        }
        this.position = newPosition % 40;
    }
    public void passGo()//để riêng vì có thể sẽ có thẻ tới ô bắt đầu
    {
        balance += 200;
        passedGoRecently = true;
        System.out.println(this.name + " đã đi qua ô GO và nhận $200");
    }
    //6.tài chính
    public void addMoney(int amount)//tăng tiền
    {
        this.balance += amount;
    }
    public boolean deductMoney(int amount)//trừ tiền
    {
        if(this.balance >= amount){ //Số dư sẽ trừ cả khi bằng lượng trừ đi
            this.balance -= amount;
            return true;
        }
        return false;
    }
    public void buyProperty(PropertySquare property)// cái này đợi gói Square
    {
        /*cái này nếu lần 1 thì chỉ được mua 1 , nếu vào trùng sau thì sẽ nâng cấp nếu muốn hay không
       với cả 4 lần nâng cấp sẽ thành khách sạn với giá cao gấp đôi và không nâng khách sạn nữa()
       có cả mua bán nhà nữa cơ, nma t vẫn chưa làm vì đợi square
        */
    }

    //7.GETTER và SETTER
    public String getName() { return name; }
    public int getBalance() { return balance; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; } // ✅ Sửa: thêm setter
    public boolean isBankrupt() { return isBankrupt; }
    public int getLastDiceRoll() { return lastDiceRoll; }
    public void setLastDiceRoll(int lastDiceRoll) { this.lastDiceRoll = lastDiceRoll; }

    //Các phương thức cho ô Jail
    public boolean isInJail() { return inJail; }
    public void setInJail(boolean inJail) { this.inJail = inJail; }
    public int getTurnsInJail() { return turnsInJail; }
    public void setTurnsInJail(int turnsInJail) { this.turnsInJail = turnsInJail; }
    public void setBankrupt(boolean isBankrupt) { this.isBankrupt = isBankrupt; }
    public Player getCreditor() { return creditor; }
    public void setCreditor(Player creditor) { this.creditor = creditor; }
    public List<PropertySquare> getOwnedProperties() { return ownedProperties; }
    public void addProperty(PropertySquare property) {
        this.ownedProperties.add(property);
    }

    //Quản lý thẻ ra tù
    public int getGetOutOfJailTicket() { return getOutOfJailFreeTickets; }
    public void addGetOutOfJailTicket() { this.getOutOfJailFreeTickets++; }
    public boolean useGetOutOfJailTicket() {
        if(this.getOutOfJailFreeTickets > 0) {
            this.getOutOfJailFreeTickets--;
            return true;
        }
        return false;
    }
}
