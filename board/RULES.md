# TÀI LIỆU LUẬT CHƠI VÀ THÔNG SỐ CỜ TỶ PHÚ (MONOPOLY)

Tài liệu quy chuẩn logic trò chơi dành cho việc phát triển các package: `board`, `square`, `player`, `dice` và `controller`.

---

## 1. Cấu Trúc Bàn Cờ (Board Layout)
Bàn cờ chuẩn gồm **40 ô** (đánh chỉ số từ `0` đến `39`, đi theo chiều kim đồng hồ):
* **Ô 0:** Khởi hành (GO).
* **Ô 10:** Ô Tù / Thăm tù (Jail / Just Visiting).
* **Ô 20:** Đỗ xe miễn phí (Free Parking).
* **Ô 30:** Vào tù (Go To Jail).
* **22 ô Đất màu (Properties):** Chia thành 8 nhóm màu.
* **4 ô Bến xe / Nhà ga (Railroads):** Ô 5, 15, 25, 35.
* **2 ô Tiện ích (Utilities):** Công ty Điện lực (ô 12) và Cấp thoát nước (ô 28).
* **3 ô Cơ hội (Chance) & 3 ô Khí vận (Community Chest):** Đặt rải rác.
* **2 ô Thuế (Tax):** Thuế thu nhập (ô 4) và Thuế xa xỉ (ô 38).

---

## 2. Di Chuyển & Xúc Xắc (Movement & Dice)
* **Đổ xúc xắc:** Lượt đi dùng 2 viên xúc xắc 6 mặt (tổng điểm từ 2 đến 12).
* **Đổ đôi (Doubles):**
    * Người chơi được đi tiếp một lượt nữa sau khi hoàn thành lượt hiện tại.
    * **Luật vi phạm tốc độ (Speeding):** Nếu đổ đôi **3 lần liên tiếp** trong cùng một vòng đi, người chơi bị tống thẳng vào tù ngay lập tức và mất lượt tiếp theo.
* **Qua ô GO (Ô 0):** Mỗi khi người chơi đi ngang qua hoặc dừng lại chính xác tại ô GO, ngân hàng cộng ngay **$200** vào tài khoản.

---

## 3. Quy Tắc Các Loại Ô (Square Types)

### 3.1. Ô Đất Màu (PropertySquare)
* **Mua đất:** Khi dừng chân tại ô đất vô chủ:
    * Người chơi có quyền chọn mua với giá niêm yết (`price`).
    * Nếu không mua: Ngân hàng mở phiên đấu giá cho toàn bộ người chơi còn lại (bắt đầu từ mức giá bất kỳ).
* **Xây nhà & Khách sạn (`houseLevel` từ 0 đến 5):**
    * Chỉ được phép xây nhà khi người chơi **sở hữu trọn bộ màu** của khu vực đó.
    * Phải xây đều tay: Không được xây 2 nhà trên một ô nếu các ô khác cùng bộ màu chưa có 1 nhà.
    * Cấp tối đa: 4 nhà thường -> Nâng cấp tiếp thành 1 Khách sạn (tương đương cấp 5).
* **Tính tiền thuê (`Rent`):**
    * Đất trống lẻ: Thu `baseRent`.
    * Đất trống nhưng **đã đủ bộ màu** (chưa xây nhà): Tiền thuê **gấp đôi** `baseRent * 2`.
    * Có nhà/khách sạn: Thu theo bảng nhân cấp độ `RENT_MULTIPLIERS = {1, 5, 15, 36, 44, 52}`.
* **Thế chấp (Mortgage):**
    * Khi cần tiền gấp, người chơi có thể thế chấp đất cho Ngân hàng để nhận lại **50% giá mua đất**.
    * Điều kiện: Phải bán hết toàn bộ nhà trên tất cả các ô thuộc bộ màu đó (bán lại cho Ngân hàng với giá 50% `houseCost`).
    * Đất đang thế chấp **không được thu tiền thuê** nếu người khác đi vào.
    * Chuộc lại đất: Trả đủ tiền thế chấp + **10% lãi**.

---

### 3.2. Ô Nhà Ga & Tiện Ích (Railroad & Utility)
* **Nhà ga (4 ô):** Tiền thuê tính theo số lượng ga người đó sở hữu:
    * 1 ga: $25
    * 2 ga: $50
    * 3 ga: $100
    * 4 ga: $200
* **Tiện ích (Điện lực / Nước):** Tiền thuê dựa trên tổng điểm xúc xắc của người vừa bước vào:
    * Sở hữu 1 tiện ích: Tiền thuê = `Điểm xúc xắc * 4`.
    * Sở hữu cả 2 tiện ích: Tiền thuê = `Điểm xúc xắc * 10`.

---

### 3.3. Ô Tù (JailSquare)
* **Dừng vào ô 10 (Thăm tù):** Không có hiệu ứng gì, người chơi chỉ là khách ghé thăm (`isJustVisiting = true`).
* **Bị tống vào tù khi:**
    1. Dừng chân tại ô 30 (Go To Jail).
    2. Bốc phải thẻ bài "Go To Jail" từ ô Cơ hội / Khí vận.
    3. Đổ đôi 3 lần liên tiếp.
       *(Khi bị vào tù, lập tức dịch chuyển về ô 10, không được nhận $200 dù có lướt qua ô GO).*
* **Quy tắc khi ở trong tù:**
    * **Vẫn được:** Thu tiền thuê nhà, tham gia đấu giá, mua bán trao đổi đất.
    * **Cách ra tù (chọn 1 trong 3):**
        1. Trả phí bảo lãnh **$50** trước khi đổ xúc xắc ở lượt của mình.
        2. Sử dụng thẻ "Get Out of Jail Free" (Thẻ miễn phí ra tù).
        3. Đổ xúc xắc: Nếu ra số đôi thì được thả tự do ngay lập tức và đi tiếp số ô vừa đổ (không được đổ thêm lượt nữa).
    * **Hết hạn 3 lượt:** Nếu sau 3 lượt cố đổ đôi không thành công, ở lượt thứ 3 bắt buộc phải trả $50 (hoặc dùng thẻ) và đi tiếp theo điểm xúc xắc vừa đổ.

---

### 3.4. Các Ô Đặc Biệt Khác
* **Ô Thuế (Tax):** Trả tiền thẳng cho Ngân hàng khi bước vào.
    * Thuế thu nhập: Trả cố định $200.
    * Thuế xa xỉ: Trả $100 (hoặc $75 tùy phiên bản).
* **Đỗ xe miễn phí (Free Parking - ô 20):** Ô an toàn, không có sự kiện gì xảy ra (một số luật chơi phụ cho phép người dừng chân nhặt toàn bộ tiền thuế/phạt tích lũy).
* **Cơ hội (Chance) & Khí vận (Community Chest):** Bốc ngẫu nhiên 1 lá bài:
    * Thưởng tiền / Phạt tiền.
    * Di chuyển tới ô chỉ định (ô GO, ga gần nhất, vào tù...).
    * Sửa chữa nhà/khách sạn (phạt tiền theo số lượng nhà đang sở hữu).
    * Thẻ miễn phí ra tù (giữ lại dùng hoặc bán lại cho người khác).

---

## 4. Giao Dịch, Phá Sản & Kết Thúc (Game Over)
* **Trao đổi / Giao dịch (Trading):**
    * Các người chơi có thể tự do thương lượng đổi đất, tiền mặt hoặc thẻ ra tù với nhau bất kỳ lúc nào giữa các lượt đi.
    * Không được giao dịch ô đất nếu trên bộ màu đó vẫn còn nhà (phải bán nhà trước).
* **Phá sản (Bankruptcy):**
    * Xảy ra khi một người chơi phải trả nợ (tiền thuê hoặc thuế) mà tổng tiền mặt + tiền bán nhà + tiền thế chấp đất không đủ bù.
    * **Nợ người chơi khác:** Toàn bộ tiền mặt và tài sản (ở trạng thái đang thế chấp) được chuyển giao hết cho chủ nợ. Người chơi bị loại.
    * **Nợ Ngân hàng:** Toàn bộ tài sản bị tịch thu, các ô đất được gỡ bỏ thế chấp và đem đấu giá công khai cho những người còn lại.
* **Điều kiện thắng:** Người duy nhất còn trụ lại sau khi toàn bộ đối thủ đã phá sản.

---

## 5. Phân Tách Trách Nhiệm Thiết Kế Hướng Đối Tượng (OOP Mapping)
* **`square`:**
    * `Square` (abstract): Quản lý vị trí, tên, hàm `applyEffect(Player p)`.
    * `PropertySquare`: Quản lý giá mua, giá nhà, cấp nhà, mảng nhân giá thuê, chủ sở hữu.
    * `JailSquare`: Phân loại ô 10 hay ô 30, logic nộp bảo lãnh, thẻ ra tù.
* **`board`:**
    * Khởi tạo mảng `Square[] board = new Square[40];` với đầy đủ thông số 40 ô.
    * Cung cấp các hàm tìm kiếm ô, kiểm tra sở hữu đủ bộ màu (`hasColorGroup`).
* **`player`:**
    * Quản lý tiền tệ, vị trí hiện tại, danh sách tài sản (`List<PropertySquare>`), trạng thái tù (`inJail`, `turnsInJail`), thẻ ra tù.
* **`controller` / `game`:**
    * Vòng lặp trò chơi (`game loop`), quản lý lượt đi, tích hợp xúc xắc, điều phối tương tác Console/UI và xử lý phá sản/đấu giá.