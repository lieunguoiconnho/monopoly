package ProjectOop.card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Bộ Bài – quản lý danh sách thẻ, xáo bài và rút bài tuần tự.
 * Khi rút hết bài thì tự xáo lại và rút từ đầu (vòng lặp bài).
 */
public class CardDeck {
    private List<Card> cards;
    private int currentIndex;

    public CardDeck(List<Card> cards) {
        this.cards = new ArrayList<>(cards);
        Collections.shuffle(this.cards);
        this.currentIndex = 0;
    }

    /** Rút 1 lá bài. Tự xáo lại nếu đã rút hết. */
    public Card draw() {
        if (currentIndex >= cards.size()) {
            Collections.shuffle(cards);
            currentIndex = 0;
        }
        return cards.get(currentIndex++);
    }

    public int size() { return cards.size(); }
}
