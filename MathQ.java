public class MathQ {


    // record current money that we win in the game
    public static int count = 0;


    // Game Rule
    // toss a coin,
    // if head shows up, we win 1 ￥,otherwise we lose 1 ￥
    // we begin with no money

    // we would throw @{num} times, record result and increase a count that
    // once our money reached 5 ￥, and finally we have 0 ￥.
    public static void play(int operator, int num, String currentRet, int currentMoney, boolean hasOver5) {
        String op;
        String ret;
        int last = currentMoney;
        int money = 0;
        if (operator == 0) {
            op = "";
        } else {
            op = operator > 0 ? "+1 " : "-1 ";
            money = operator > 0 ?  1 :  -1;
        }
        ret = currentRet.concat(op);

        last += money;
        if (num == 0) {
            System.out.println(ret);
            if (last == 0 && hasOver5 /* reach 5 */) {
                count++;
            }

            return;
        }

        num--;
        currentMoney = last;
        currentRet = ret;
        if (currentMoney == 5) {
            hasOver5 = true;
        }

        play(1, num, currentRet,currentMoney,hasOver5);
        play(-1, num, currentRet,currentMoney,hasOver5);
    }
    
        public static void main(String[] args) {
        p(0,20,"一条路线: ",0,false);
        System.out.println("满足次数： " + count);
    }
}
