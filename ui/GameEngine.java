package ProjectOop.ui;

import ProjectOop.board.Board;
import ProjectOop.dice.Dice;
import ProjectOop.player.Player;
import ProjectOop.square.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Game Engine – điều phối logic game theo mô hình event-driven.
 * Không chứa I/O, chỉ notify qua GameListener để UI phản ứng.
 */
public class GameEngine {

    // =========================================================
    // PHASE & LISTENER
    // =========================================================
    public enum TurnPhase {
        WAITING_ROLL,          // Chờ người chơi đổ xúc xắc
        WAITING_JAIL_ACTION,   // Chờ quyết định khi đang tù
        WAITING_BUY,           // Chờ quyết định mua đất
        WAITING_UPGRADE,       // Chờ quyết định nâng cấp
        GAME_OVER
    }

    public interface GameListener {
        void onGameStarted();
        void onTurnStart(Player player, TurnPhase phase);
        void onDiceRolled(int d1, int d2, boolean isDouble);
        void onPlayerMoved(Player player, int from, int to);
        void onSquareEffect(Player player, String squareName, String message);
        void onBuyPrompt(Player player, PropertySquare property);
        void onJailPrompt(Player player);
        void onUpgradePrompt(Player player, List<PropertySquare> upgradeable);
        void onCardDrawn(Player player, String cardType, ProjectOop.card.Card card);
        void onRentPaid(Player player, Player owner, PropertySquare property, int rent);
        void onBankruptcy(Player bankrupt, Player creditor);
        void onGameOver(Player winner);
        void onMessage(String msg);
        void onBoardUpdated();
    }

    // =========================================================
    // STATE
    // =========================================================
    private final Board board;
    private final Player[] players;
    private final Dice dice1, dice2;
    private final List<GameListener> listeners = new ArrayList<>();

    private int currentPlayerIndex;
    private int consecutiveDoubles;
    private TurnPhase phase;

    // Persist across buy/upgrade prompts
    private int lastD1, lastD2;
    private boolean lastIsDouble;
    private boolean freedFromJailThisTurn;

    // =========================================================
    // INIT
    // =========================================================
    public GameEngine(String[] playerNames) {
        board = new Board();
        dice1 = new Dice();
        dice2 = new Dice();
        players = new Player[playerNames.length];
        for (int i = 0; i < playerNames.length; i++) {
            players[i] = new Player(playerNames[i]);
        }
        currentPlayerIndex = 0;
        phase = TurnPhase.WAITING_ROLL;
    }

    public void addListener(GameListener l) { listeners.add(l); }

    public void startGame() {
        emit(l -> l.onGameStarted());
        startTurn();
    }

    // =========================================================
    // ACTIONS (called by UI buttons)
    // =========================================================

    /** Người chơi nhấn "Đổ Xúc Xắc" */
    public void actionRoll() {
        if (phase != TurnPhase.WAITING_ROLL) return;

        lastD1 = dice1.roll();
        lastD2 = dice2.roll();
        int total = lastD1 + lastD2;
        lastIsDouble = (lastD1 == lastD2);

        emit(l -> l.onDiceRolled(lastD1, lastD2, lastIsDouble));

        // 3 đôi liên tiếp → vào tù
        if (lastIsDouble) {
            consecutiveDoubles++;
            if (consecutiveDoubles >= 3) {
                msg("Đổ đôi 3 lần liên tiếp! " + getCurrentPlayer().getName() + " bị vào tù!");
                sendToJail(getCurrentPlayer());
                endTurnAndNext();
                return;
            }
        }

        moveAndApply(getCurrentPlayer(), total);
    }

    /** Người chơi chọn hành động khi ở tù (1=nộp tiền, 2=dùng thẻ, 3=thử đổ đôi) */
    public void actionJail(int choice) {
        if (phase != TurnPhase.WAITING_JAIL_ACTION) return;
        Player p = getCurrentPlayer();

        if (choice == 1) { // Nộp tiền bảo lãnh
            if (p.deductMoney(JailSquare.BAIL_AMOUNT)) {
                p.setInJail(false);
                p.setTurnsInJail(0);
                freedFromJailThisTurn = true;
                msg(p.getName() + " nộp $" + JailSquare.BAIL_AMOUNT + " → ra tù!");
                phase = TurnPhase.WAITING_ROLL;
                emit(l -> l.onTurnStart(p, phase));
            } else {
                msg("Không đủ tiền bảo lãnh! Thử đổ đôi...");
                actionJail(3);
            }
        } else if (choice == 2) { // Dùng thẻ ra tù
            if (p.useGetOutOfJailTicket()) {
                p.setInJail(false);
                p.setTurnsInJail(0);
                freedFromJailThisTurn = true;
                msg(p.getName() + " dùng thẻ Ra Tù Miễn Phí!");
                phase = TurnPhase.WAITING_ROLL;
                emit(l -> l.onTurnStart(p, phase));
            } else {
                msg("Không có thẻ! Thử đổ đôi...");
                actionJail(3);
            }
        } else { // Thử đổ đôi
            lastD1 = dice1.roll();
            lastD2 = dice2.roll();
            int total = lastD1 + lastD2;
            lastIsDouble = (lastD1 == lastD2);
            emit(l -> l.onDiceRolled(lastD1, lastD2, lastIsDouble));

            p.setTurnsInJail(p.getTurnsInJail() + 1);

            if (lastIsDouble) {
                p.setInJail(false);
                p.setTurnsInJail(0);
                freedFromJailThisTurn = true;
                msg("Ra tù bằng đôi! Đi " + total + " bước.");
                moveAndApply(p, total);
            } else if (p.getTurnsInJail() >= 3) {
                msg("Hết 3 lượt! Bắt buộc nộp $" + JailSquare.BAIL_AMOUNT + ".");
                if (!p.deductMoney(JailSquare.BAIL_AMOUNT)) {
                    p.setCreditor(null);
                    p.setBankrupt(true);
                    handleBankruptcy(p);
                    return;
                }
                p.setInJail(false);
                p.setTurnsInJail(0);
                freedFromJailThisTurn = true;
                moveAndApply(p, total);
            } else {
                msg("Không ra đôi. Còn " + (3 - p.getTurnsInJail()) + " lượt thử.");
                endTurnAndNext();
            }
        }
    }

    /** Người chơi quyết định mua (true) hoặc bỏ qua (false) */
    public void actionBuy(boolean doBuy) {
        if (phase != TurnPhase.WAITING_BUY) return;
        Player p = getCurrentPlayer();
        Square sq = board.getSquare(p.getPosition());

        if (doBuy && sq instanceof PropertySquare) {
            PropertySquare prop = (PropertySquare) sq;
            if (prop.buyProperty(p)) {
                board.updateColorGroups();
                msg("✅ " + p.getName() + " mua " + prop.getName() + " ($" + prop.getPrice() + ") [Đất trống - Cấp 0]");
                emit(l -> l.onBoardUpdated());
            }
        } else if (!doBuy && sq instanceof PropertySquare) {
            msg(p.getName() + " quyết định không mua " + sq.getName());
        }
        finishRollPhase();
    }

    /** Người chơi chọn nâng cấp ô đang đứng (idx >= 0 = nâng cấp 1 lần, -1 = bỏ qua) */
    public void actionUpgrade(int idx) {
        if (phase != TurnPhase.WAITING_UPGRADE) return;
        Player p = getCurrentPlayer();
        Square sq = board.getSquare(p.getPosition());

        if (idx >= 0 && sq instanceof PropertySquare) {
            PropertySquare prop = (PropertySquare) sq;
            if (prop.getOwner() == p && prop.canUpgrade(p)) {
                if (prop.upgrade(p)) {
                    String lvl = prop.getHouseLevel() == 5 ? "🏨 Khách sạn" : "🏠 Cấp " + prop.getHouseLevel();
                    msg("✅ " + p.getName() + " nâng cấp " + prop.getName() + " → " + lvl);
                    emit(l -> l.onBoardUpdated());
                }
            }
        } else {
            msg(p.getName() + " bỏ qua nâng cấp lượt này.");
        }
        // Mỗi lần vào chỉ được nâng cấp tối đa 1 lần → Kết thúc lượt
        finishRollPhase();
    }

    // =========================================================
    // INTERNAL
    // =========================================================
    private void startTurn() {
        Player p = getCurrentPlayer();
        consecutiveDoubles = 0;
        freedFromJailThisTurn = false;

        if (p.isInJail()) {
            phase = TurnPhase.WAITING_JAIL_ACTION;
            emit(l -> l.onJailPrompt(p));
        } else {
            phase = TurnPhase.WAITING_ROLL;
            emit(l -> l.onTurnStart(p, phase));
        }
    }

    private void moveAndApply(Player p, int steps) {
        int from = p.getPosition();
        p.setLastDiceRoll(steps);
        p.move(steps);
        int to = p.getPosition();
        emit(l -> l.onPlayerMoved(p, from, to));

        applySquareAtCurrentPosition(p);
    }

    private void applySquareAtCurrentPosition(Player p) {
        int curPos = p.getPosition();
        Square sq = board.getSquare(curPos);

        String sqName = sq.getName();
        emit(l -> l.onSquareEffect(p, sqName, p.getName() + " dừng tại: " + sqName));
        emit(l -> l.onBoardUpdated());

        int posBefore = p.getPosition();
        sq.applyEffect(p);
        board.updateColorGroups();
        emit(l -> l.onBoardUpdated());

        if (p.isBankrupt()) { handleBankruptcy(p); return; }

        // Thông báo tiền thuê nếu bước vào ô đất của người khác
        if (sq instanceof PropertySquare) {
            PropertySquare prop = (PropertySquare) sq;
            if (prop.getOwner() != null && prop.getOwner() != p) {
                int rent = (prop instanceof UtilitySquare)
                        ? ((UtilitySquare) prop).getRent(p.getLastDiceRoll())
                        : prop.getRent();
                emit(l -> l.onRentPaid(p, prop.getOwner(), prop, rent));
                msg("🏠 " + p.getName() + " vào ô của " + prop.getOwner().getName() + " (" + prop.getName() + ") → Trả tiền thuê: $" + rent);
            }
        }

        // Thông báo nội dung thẻ bài nếu vừa rút thẻ Cơ Hội / Khí Vận
        if (sq instanceof ChanceSquare) {
            ProjectOop.card.Card c = ((ChanceSquare) sq).getLastDrawnCard();
            if (c != null) {
                msg("⭐ [Cơ Hội]: " + c.getDescription());
                emit(l -> l.onCardDrawn(p, "Cơ Hội", c));
            }
        } else if (sq instanceof CommunityChestSquare) {
            ProjectOop.card.Card c = ((CommunityChestSquare) sq).getLastDrawnCard();
            if (c != null) {
                msg("🎁 [Khí Vận]: " + c.getDescription());
                emit(l -> l.onCardDrawn(p, "Khí Vận", c));
            }
        }

        // Kiểm tra xem thẻ hoặc hiệu ứng có làm dịch chuyển vị trí không (ví dụ: thẻ bay tới Lê Lợi, vào tù, lùi...)
        int posAfter = p.getPosition();
        if (posAfter != posBefore) {
            emit(l -> l.onPlayerMoved(p, posBefore, posAfter));
            emit(l -> l.onBoardUpdated());

            if (p.isInJail()) {
                msg("🔒 " + p.getName() + " bị vào tù!");
                finishRollPhase();
                return;
            }

            // Tiếp tục áp dụng hiệu ứng của ô mới mà người chơi vừa được đưa tới
            applySquareAtCurrentPosition(p);
            return;
        }

        // Lấy chính xác ô tại vị trí thực tế sau khi đã xử lý xong dịch chuyển
        Square finalSq = board.getSquare(p.getPosition());

        // Xử lý ô đất:
        // 1. Nếu ô đất trống → Quyết định mua đất
        // 2. Nếu đã là chủ sở hữu ô đó → Quyết định nâng cấp nếu đủ điều kiện
        if (finalSq instanceof PropertySquare) {
            PropertySquare prop = (PropertySquare) finalSq;
            if (prop.getOwner() == null) {
                if (p.getBalance() >= prop.getPrice()) {
                    phase = TurnPhase.WAITING_BUY;
                    emit(l -> l.onBuyPrompt(p, prop));
                    return;
                } else {
                    msg("💸 " + p.getName() + " không đủ $" + prop.getPrice() + " để mua " + prop.getName() + " (Số dư: $" + p.getBalance() + ").");
                }
            } else if (prop.getOwner() == p) {
                if (prop.canUpgrade(p)) {
                    phase = TurnPhase.WAITING_UPGRADE;
                    List<PropertySquare> list = new ArrayList<>();
                    list.add(prop);
                    emit(l -> l.onUpgradePrompt(p, list));
                    return;
                } else if (prop.getHouseCost() > 0) {
                    if (prop.getHouseLevel() >= 5) {
                        msg("🏨 " + prop.getName() + " đã đạt cấp tối đa (Khách sạn).");
                    } else if (!prop.isColorGroupComplete()) {
                        msg("ℹ " + p.getName() + " cần sở hữu trọn bộ màu " + (prop.getColorGroup() != null ? prop.getColorGroup().name() : "") + " để xây nhà trên " + prop.getName());
                    } else if (p.getBalance() < prop.getHouseCost()) {
                        msg("💸 " + p.getName() + " không đủ $" + prop.getHouseCost() + " để nâng cấp " + prop.getName());
                    }
                }
            }
        }
        finishRollPhase();
    }

    private void finishRollPhase() {
        Player p = getCurrentPlayer();
        // Đổ đôi → đi thêm lượt (trừ khi vừa thoát tù hoặc đang ở tù)
        if (lastIsDouble && !freedFromJailThisTurn && !p.isInJail()) {
            msg("⭐ Đổ đôi! " + p.getName() + " được đi thêm lượt.");
            phase = TurnPhase.WAITING_ROLL;
            emit(l -> l.onTurnStart(p, phase));
        } else {
            endTurnAndNext();
        }
    }

    private void endTurnAndNext() {
        int count = countActive();
        if (count <= 1) { announceWinner(); return; }
        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.length;
        } while (players[currentPlayerIndex].isBankrupt());
        startTurn();
    }

    private void sendToJail(Player p) {
        p.setPosition(JailSquare.JAIL_POSITION);
        p.setInJail(true);
        p.setTurnsInJail(0);
        emit(l -> l.onBoardUpdated());
    }

    private void handleBankruptcy(Player p) {
        Player creditor = p.getCreditor();
        if (creditor != null) {
            creditor.addMoney(p.getBalance());
            p.deductMoney(p.getBalance());
            for (PropertySquare prop : p.getOwnedProperties()) prop.transferPropertyTo(creditor);
        } else {
            for (PropertySquare prop : p.getOwnedProperties()) prop.releaseProperty();
        }
        p.getOwnedProperties().clear();
        board.updateColorGroups();

        emit(l -> l.onBankruptcy(p, creditor));
        emit(l -> l.onBoardUpdated());

        if (countActive() <= 1) { announceWinner(); return; }
        endTurnAndNext();
    }

    private void announceWinner() {
        phase = TurnPhase.GAME_OVER;
        Player winner = getWinner();
        emit(l -> l.onGameOver(winner));
    }

    private List<PropertySquare> getUpgradeable(Player p) {
        List<PropertySquare> res = new ArrayList<>();
        for (PropertySquare prop : p.getOwnedProperties()) {
            if (prop.canUpgrade(p)) res.add(prop);
        }
        return res;
    }

    private int countActive() {
        int n = 0;
        for (Player p : players) if (!p.isBankrupt()) n++;
        return n;
    }

    private Player getWinner() {
        for (Player p : players) if (!p.isBankrupt()) return p;
        return null;
    }

    private void msg(String m) { emit(l -> l.onMessage(m)); }

    @FunctionalInterface
    private interface ListenerAction { void call(GameListener l); }
    private void emit(ListenerAction a) { for (GameListener l : listeners) a.call(l); }

    // =========================================================
    // GETTERS
    // =========================================================
    public Player getCurrentPlayer() { return players[currentPlayerIndex]; }
    public Player[] getPlayers() { return players; }
    public Board getBoard() { return board; }
    public TurnPhase getPhase() { return phase; }
    public int getLastD1() { return lastD1; }
    public int getLastD2() { return lastD2; }
}
