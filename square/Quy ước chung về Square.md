# Hướng Dẫn Kỹ Thuật Package Square

Package `ProjectOop.square` quản lý toàn bộ 40 ô trên bàn cờ Monopoly. Mọi loại ô đều kế thừa lớp trừu tượng `Square`.

---

## 1. Nguyên Tắc Cốt Lõi Khi Viết Ô Mới

* **Phân định rõ hiệu ứng bắt buộc và hành động tự chọn:**
* `applyEffect(Player p)`: Chỉ thực hiện những tác động bắt buộc xảy ra ngay khi chân vừa chạm ô (bị trừ thuế, trả tiền thuê, bị bắt vào tù, rút thẻ bài).
* **Hành động chủ động:** Các tương tác như mua đất, nâng cấp nhà, nộp tiền bảo lãnh... phải tách thành các hàm nghiệp vụ riêng như `buyProperty`, `upgrade`, `payBail`. `GameController` hoặc giao diện người dùng sẽ hỏi người chơi trước rồi mới gọi các hàm này.


* **Không đưa I/O vào class:** Tuyệt đối không dùng `Scanner`, không gắn giao diện đồ họa hoặc nhập xuất bàn phím trực tiếp bên trong các ô. Package này chỉ chứa dữ liệu và logic thuần túy.

---

## 2. Trạng Thái Hiện Tại Của Package

* `Square`: Lớp cha trừu tượng lưu vị trí (`position`), tên (`name`) và phương thức trừu tượng `applyEffect(Player p)`.
* `PropertySquare`: Quản lý đất màu, cấp nhà (0 đến 5), hệ số tiền thuê và các hàm mua đất, nâng cấp nhà.
* `JailSquare`: Phân biệt ô Vào tù và ô Thăm tù, các hàm nộp tiền bảo lãnh và dùng thẻ ra tù.

---

## 3. Đặc Tả Chi Tiết Các Ô Cần Bổ Sung

### TaxSquare (Ô Thuế)

* **Vị trí:** Ô 4 (Thuế thu nhập - $200), Ô 38 (Thuế xa xỉ - $100).
* **Thuộc tính:** `taxAmount` (số tiền thuế quy định).
* **Cơ chế:** Trong `applyEffect`, trừ tiền người chơi bằng `deductMoney(taxAmount)`. Nếu không đủ số dư, trừ toàn bộ số tiền còn lại của người chơi và kích hoạt cờ phá sản bằng `setBankrupt(true)`.

### RailroadSquare (Ô Nhà Ga)

* **Vị trí:** 4 ô tại các vị trí 5, 15, 25, 35.
* **Thuộc tính:** `price` ($200), `owner` (`Player`).
* **Cơ chế tiền thuê:** Thu theo số lượng nhà ga mà chủ sở hữu đang nắm giữ: 1 ga ($25), 2 ga ($50), 3 ga ($100), 4 ga ($200). Công thức tính nhanh: `25 * Math.pow(2, count - 1)`.
* **Các hàm cần viết:**
* `applyEffect`: Kiểm tra nếu ô đã có chủ và người dừng chân không phải là chủ sở hữu thì tính tiền thuê theo số ga người chủ nắm giữ, sau đó trừ tiền khách và cộng tiền cho chủ.
* `buyRailroad`: Kiểm tra số dư người mua, trừ tiền, gán `owner` và thêm ô này vào danh sách tài sản của người chơi.



### UtilitySquare (Ô Tiện Ích)

* **Vị trí:** Ô 12 (Công ty Điện), Ô 28 (Công ty Nước).
* **Thuộc tính:** `price` ($150), `owner` (`Player`).
* **Cơ chế tiền thuê:** Phụ thuộc vào điểm xúc xắc vừa đổ của người đi vào:
* Chủ sở hữu có 1 tiện ích: Tiền thuê = Điểm xúc xắc × 4.
* Chủ sở hữu có cả 2 tiện ích: Tiền thuê = Điểm xúc xắc × 10.


* **Các hàm cần viết:**
* `applyEffect`: Lấy điểm xúc xắc từ `p.getLastDiceRoll()`, nhân với hệ số tương ứng rồi thực hiện giao dịch chuyển tiền giữa 2 người chơi.
* `buyUtility`: Hàm mua ô tiện ích tương tự như mua nhà ga.



### ChanceSquare & CommunityChestSquare (Cơ Hội & Khí Vận)
* Hoặc 1 ChanceSquare dùng 1 biến boolean cho cả hai ô.
* **Vị trí:** Rải rác trên bàn cờ (các ô 2, 7, 17, 22, 33, 36).
* **Thuộc tính:** Giữ một tham chiếu tới bộ bài rút chung (`CardDeck`).
* **Cơ chế:** Trong `applyEffect`, gọi lệnh rút 1 lá bài ngẫu nhiên từ bộ bài và kích hoạt hiệu ứng của lá bài đó lên người chơi (thưởng/phạt tiền, dịch chuyển vị trí, nhận thẻ ra tù).

### SpecialSquare (Khởi Hành, Đỗ Xe)

* **Vị trí:** Ô 0 (Khởi hành - GO), Ô 20 (Đỗ xe miễn phí - Free Parking).
* **Thuộc tính:** Chỉ dùng vị trí và tên, không có giá mua và không có chủ sở hữu.
* **Cơ chế:** Hàm `applyEffect` để trống. Tiền thưởng $200 khi đi qua ô GO được xử lý tại vòng lặp di chuyển của bàn cờ chứ không kích hoạt riêng tại ô 0.

---
