package ProjectOop.square;

/**
 * Enum đại diện cho 8 nhóm màu trên bàn cờ Monopoly.
 * Board sẽ dùng enum này để kiểm tra đủ bộ màu (hasColorGroup).
 */
public enum ColorGroup {
    PURPLE,     // Tím  (2 ô: 1, 3)
    LIGHT_BLUE, // Xanh nhạt (3 ô: 6, 8, 9)
    PINK,       // Hồng (3 ô: 11, 13, 14)
    ORANGE,     // Cam  (3 ô: 16, 18, 19)
    RED,        // Đỏ   (3 ô: 21, 23, 24)
    YELLOW,     // Vàng (3 ô: 26, 27, 29)
    GREEN,      // Xanh lá (3 ô: 31, 32, 34)
    DARK_BLUE   // Xanh đậm (2 ô: 37, 39)
}
