# 🎲 Code Review – Game Cờ Tỷ Phú (OOP Java)

> Trạng thái tổng thể: **Nền móng khá tốt**, nhưng còn nhiều điểm cần sửa trước khi phát triển tiếp.

---

## ✅ Những gì đã làm tốt

- Phân chia package hợp lý (`dice`, `player`, `square`).
- `Square` là abstract class đúng cách, dùng `applyEffect(Player p)` theo đúng nguyên lý.
- `PropertySquare` có logic mua đất, nâng cấp, tính tiền thuê rõ ràng.
- `JailSquare` phân tách đúng 2 vai trò: ô 10 (thăm tù) và ô 30 (vào tù).
- `UtilitySquare` kế thừa `PropertySquare` và override `getRent`, `canUpgrade` đúng cách.
- Có tài liệu RULES.md rõ ràng, chi tiết — rất tốt cho teamwork.

---

## ❌ Các Vấn Đề Cần Sửa

### 1. 🔴 [dice.java] – Tên class sai convention + Xúc xắc chỉ 1 viên

**File:** [`dice.java`](file:///d:/Code/Github/OOP%20Project/ProjectOop/ProjectOop/dice/dice.java)

```java
// ❌ Hiện tại
public class dice {           // Tên class phải viết hoa chữ cái đầu
    public int roll() {
        return (int)(Math.random() * 6) + 1;  // Chỉ 1 viên xúc xắc!
    }
}
```

**Vấn đề:**
- Tên class `dice` vi phạm Java Naming Convention → phải là `Dice`.
- Game Monopoly cần **2 viên xúc xắc** (tổng từ 2–12), không phải 1 viên (1–6).
- Không có cơ chế phát hiện **đổ đôi (doubles)** — điều cực kỳ quan trọng (lượt thêm, vào tù sau 3 đôi).

```java
// ✅ Nên sửa thành
public class Dice {
    private int die1, die2;

    public int roll() {
        die1 = (int)(Math.random() * 6) + 1;
        die2 = (int)(Math.random() * 6) + 1;
        return die1 + die2;
    }

    public boolean isDouble() {
        return die1 == die2;
    }

    public int getDie1() { return die1; }
    public int getDie2() { return die2; }
}
```

---

### 2. 🔴 [Player.java] – `name` và `position` là `public`, vi phạm Encapsulation

**File:** [`Player.java`](file:///d:/Code/Github/OOP%20Project/ProjectOop/ProjectOop/player/Player.java#L10-L12)

```java
// ❌ Hiện tại
public String name;     // L10
public int position;    // L12
```

Hai trường này bị để `public`, ai cũng có thể truy cập và thay đổi trực tiếp — vi phạm nguyên tắc Encapsulation cơ bản của OOP.

> Ví dụ xấu: `JailSquare.java` L23 đang làm `p.position = JAIL_POSITION;` thay vì dùng setter!

```java
// ✅ Nên sửa
private String name;
private int position;
// Thêm setter cho position:
public void setPosition(int position) { this.position = position; }
```

---

### 3. 🔴 [Player.java] – `move()` không xử lý trường hợp đang ở trong tù

**File:** [`Player.java`](file:///d:/Code/Github/OOP%20Project/ProjectOop/ProjectOop/player/Player.java#L37-L44)

```java
// ❌ Hiện tại – khi đang ở tù vẫn di chuyển bình thường
public void move(int step) {
    int newPosition = this.position + step;
    if(newPosition >= 40) { passGo(); }
    this.position = newPosition % 40;
}
```

Khi `inJail == true`, người chơi **không được di chuyển** (trừ khi đã thoát tù). Logic này cần được kiểm tra.

```java
// ✅ Gợi ý sửa
public void move(int step) {
    if (inJail) return; // Không di chuyển khi đang ở tù
    int newPosition = this.position + step;
    if(newPosition >= 40) { passGo(); }
    this.position = newPosition % 40;
}
```

---

### 4. 🔴 [Player.java] – `getOutOfJailFreeTickets` chưa khởi tạo

**File:** [`Player.java`](file:///d:/Code/Github/OOP%20Project/ProjectOop/ProjectOop/player/Player.java#L18)

```java
private int getOutOfJailFreeTickets; // ❌ Không khởi tạo trong constructor
```

Mặc dù Java khởi tạo `int` về `0` mặc định, nhưng không khai báo tường minh trong constructor là **thiếu rõ ràng**, gây nhầm lẫn khi đọc code.

```java
// ✅ Thêm vào constructor
this.getOutOfJailFreeTickets = 0;
```

---

### 5. 🟡 [PropertySquare.java] – `getRent()` chưa xử lý trường hợp "đủ bộ màu"

**File:** [`PropertySquare.java`](file:///d:/Code/Github/OOP%20Project/ProjectOop/ProjectOop/square/PropertySquare.java#L25-L28)

Theo RULES.md (mục 3.1):
> *"Đất trống nhưng đã đủ bộ màu (chưa xây nhà): Tiền thuê gấp đôi `baseRent * 2`."*

```java
// ❌ Hiện tại không xét trường hợp này
public int getRent() {
    if (houseLevel < 0 || houseLevel >= RENT_MULTIPLIERS.length) { return baseRent; }
    return baseRent * RENT_MULTIPLIERS[houseLevel]; // RENT_MULTIPLIERS[0] = 1 → chỉ trả baseRent*1
}
```

Khi `houseLevel == 0` và chủ sở hữu đã đủ bộ màu → tiền thuê phải là `baseRent * 2`, nhưng hiện tại chỉ trả `baseRent * 1`. Cần thêm cờ `isColorGroupComplete` hoặc truyền tham số từ `Board`.

---

### 6. 🟡 [PropertySquare.java] – Logic phá sản trong `applyEffect` không đúng

**File:** [`PropertySquare.java`](file:///d:/Code/Github/OOP%20Project/ProjectOop/ProjectOop/square/PropertySquare.java#L33-L43)

```java
// ❌ Vấn đề: deductMoney(p.getBalance()) được gọi 2 lần!
else {
    owner.addMoney(p.getBalance());
    p.deductMoney(p.getBalance()); // Gọi xong balance = 0, nhưng...
    p.setBankrupt(true);
}
```

Thứ tự này có vẻ ổn, nhưng tương tự logic xuất hiện ở cả `UtilitySquare.java` — nên **tách thành hàm riêng** để tránh trùng lặp (DRY principle).

---

### 7. 🟡 [UtilitySquare.java] – Override sai signature của `getRent`

**File:** [`UtilitySquare.java`](file:///d:/Code/Github/OOP%20Project/ProjectOop/ProjectOop/square/UtilitySquare.java#L18-L19)

```java
// ❌ Override nhưng signature khác → không thực sự override!
@Override
public int getRent(int diceRoll) { ... }   // Có tham số

// PropertySquare cha có:
public int getRent() { ... }               // Không có tham số
```

Đây là **overloading**, không phải **overriding**! Annotation `@Override` sẽ gây **lỗi compile** vì method `getRent(int)` không tồn tại trong lớp cha. Cần quyết định:
- Thêm `getRent(int diceRoll)` vào lớp cha `PropertySquare` (và `Square`).
- Hoặc không dùng `@Override` và gọi trực tiếp từ `applyEffect`.

---

### 8. 🟡 [ChanceSquare.java] – Thiếu dấu cách trong chuỗi in ra

**File:** [`ChanceSquare.java`](file:///d:/Code/Github/OOP%20Project/ProjectOop/ProjectOop/square/ChanceSquare.java#L12)

```java
// ❌ "Playerlanded on..." — thiếu dấu cách
System.out.println(p.getName() + "landed on" + name);

// ✅ Sửa lại
System.out.println(p.getName() + " landed on " + name);
```

Ngoài ra, `ChanceSquare` hiện chỉ là stub — chưa có logic thẻ bài, bộ `CardDeck` như tài liệu quy định.

---

### 9. 🟡 [GameController.java] – Không phải là `main` đúng nghĩa, mới chỉ là test

**File:** [`GameController.java`](file:///d:/Code/Github/OOP%20Project/ProjectOop/ProjectOop/GameController.java)

```java
// ❌ Tên tham số sai convention
public static void main(String[] arrgs)  // "arrgs" thay vì "args"

// ❌ Không có Board, không có game loop, không có danh sách players
```

`GameController` hiện chỉ là đoạn test nhỏ. Cần xây dựng:
- Khởi tạo `Board` với 40 ô.
- Danh sách `Player[]`.
- Vòng lặp game loop.

---

### 10. 🟡 [Thiếu class] – Chưa có `Board`, `RailroadSquare`, `TaxSquare`, `CardDeck`

Theo tài liệu `RULES.md` và `Quy ước chung về Square.md`, còn thiếu:

| Class cần tạo | Mức độ ưu tiên |
|---|---|
| `Board.java` | 🔴 Cao – cần ngay cho game loop |
| `RailroadSquare.java` | 🟡 Trung bình |
| `TaxSquare.java` | 🟡 Trung bình |
| `SpecialSquare.java` (GO, Free Parking) | 🟡 Trung bình |
| `CardDeck.java` + `Card.java` | 🟠 Thấp hơn |
| `CommunityChestSquare.java` | 🟠 Thấp hơn |

---

## 📋 Tổng Kết

| Mức độ | Vấn đề |
|---|---|
| 🔴 Nghiêm trọng | `dice` 1 viên, thiếu doubles; `name`/`position` public; `move()` không check jail |
| 🔴 Lỗi compile | `@Override getRent(int)` ở `UtilitySquare` sai signature |
| 🟡 Logic sai | `getRent()` thiếu case đủ bộ màu; `getOutOfJailFreeTickets` chưa init; typo ChanceSquare |
| 🟡 Thiếu class | `Board`, `RailroadSquare`, `TaxSquare`, `CardDeck` |
| 🟢 Gợi ý cải tiến | Tách hàm xử lý phá sản dùng chung (DRY); Thêm `setPosition()` thay vì truy cập trực tiếp |

