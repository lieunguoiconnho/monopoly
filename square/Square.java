package ProjectOop.square;

import ProjectOop.player.*;

public abstract class Square {
    protected int position; // Vị trí ô đất
    protected String name; // Tên ô đất

    //1. Constructor, Square khác gọi qua hàm super()
    public Square(int position, String name) {
        this.position = position;
        this.name = name;
    }

    //2. Hàm abstract xử lý khi người chơi dừng chân tại ô này
    public abstract void applyEffect(Player p);

    //3. GETTER và SETTER (chưa cần SETTER ở đây)
    public int getPosition() { return position; }
    public String getName() { return name; }
}
