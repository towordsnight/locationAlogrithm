import java.util.*;

public class Location {
    private double preX;
    private double preY;
//接收信号强度向量
    private Integer ss1;
    private Integer ss2;
    private Integer ss3;
    private Integer ss4;
    private Integer ss5;

    List<Integer> location = new ArrayList<>();

    public Location(Integer ss1,Integer ss2,Integer ss3,Integer ss4,Integer ss5){
        this.ss1 = ss1;
        this.ss2 = ss2;
        this.ss3 = ss3;
        this.ss4 = ss4;
        this.ss5 = ss5;
    }

    public void init(){
        // 如果传回的参数不是向量，而是一个locaTion对象，将其转换为list
        location.add(ss1);
        location.add(ss2);
        location.add(ss3);
        location.add(ss4);
        location.add(ss5);
    }

    /**
     * 计算两个list的余弦相似度
     * @param pre_plot_RSSI 定位点信号强度向量
     * @param coreRSSI 质心信号强度向量
     * */
    public double calSimilarity(List<Integer> pre_plot_RSSI, List<Double> coreRSSI){
        // pre_plot_RSSI.size()只是五个信号强度的向量；refer_RSSI.size()多出x,y坐标
        if (pre_plot_RSSI.size() > coreRSSI.size()-2) {
            int temp = pre_plot_RSSI.size() - coreRSSI.size();
            for (int i = 0; i < temp; i++) {
                coreRSSI.add(0.0);
            }
        } else if (pre_plot_RSSI.size() < coreRSSI.size()-2) {
            int temp = coreRSSI.size() - pre_plot_RSSI.size();
            for (int i = 0; i < temp; i++) {
                pre_plot_RSSI.add(0);
            }
        }
        // 两个向量维度相同
        // 余弦相似度
        double simVal = 0;
        // 向量点积
        double num = 0;
        // 向量长度之积
        double den = 1;

        double powa_sum = 0;
        double powb_sum = 0;
        for (int i = 0; i < coreRSSI.size()-2; i++) {
            double a = Double.parseDouble(pre_plot_RSSI.get(i).toString());
            double b = Double.parseDouble(coreRSSI.get(i+2).toString());
            num = num + a * b;
            powa_sum = powa_sum + (double) Math.pow(a, 2);
            powb_sum = powb_sum + (double) Math.pow(b, 2);
        }
        double sqrta = (double) Math.sqrt(powa_sum);
        double sqrtb = (double) Math.sqrt(powb_sum);
        den = sqrta * sqrtb;
        simVal = num / den;
//        System.out.println("的余弦距离是simval:"+simVal);
        return simVal;
    }

    /**
     * 参数type List<Double>和List<Integer>无法统一，只能重写一个
     * */
    public double calReferFPSimilarity(List<Integer> pre_plot_RSSI, List<Integer> refer_RSSI){
        // pre_plot_RSSI.size()只是五个信号强度的向量；refer_RSSI.size()多出x,y坐标
        if (pre_plot_RSSI.size() > refer_RSSI.size()-2) {
            int temp = pre_plot_RSSI.size() - refer_RSSI.size();
            for (int i = 0; i < temp; i++) {
                refer_RSSI.add(0);
            }
        } else if (pre_plot_RSSI.size() < refer_RSSI.size()-2) {
            int temp = refer_RSSI.size() - pre_plot_RSSI.size();
            for (int i = 0; i < temp; i++) {
                pre_plot_RSSI.add(0);
            }
        }
        // 两个向量维度相同
        // 余弦相似度
        double simVal = 0;
        // 向量点积
        double num = 0;
        // 向量长度之积
        double den = 1;

        double powa_sum = 0;
        double powb_sum = 0;
        for (int i = 0; i < refer_RSSI.size()-2; i++) {
            double a = Double.parseDouble(pre_plot_RSSI.get(i).toString());
            double b = Double.parseDouble(refer_RSSI.get(i+2).toString());
            num = num + a * b;
            powa_sum = powa_sum + (double) Math.pow(a, 2);
            powb_sum = powb_sum + (double) Math.pow(b, 2);
        }
        double sqrta = (double) Math.sqrt(powa_sum);
        double sqrtb = (double) Math.sqrt(powb_sum);
        den = sqrta * sqrtb;
        simVal = num / den;
//        System.out.println("的余弦距离是simval:"+simVal);
        return simVal;
    }

    /**
     * 计算和所有质心余弦相似度
     *
     * @param cores 数据中所有质心
     *
     * 返回值 1.最大余弦相似度 2.距离最近的质心 3.所属类
     * 返回与定位点相似的质心所属的类
     */
//    public Integer getSimilarityGroup(List<Integer> pre_plot_RSSI, List<List<Double>> cores) {
    public Integer getSimilarityGroup(List<List<Double>> cores) {
        // 定位点与所有质心的余弦距离
        List<Double> cosDiffWithCore = new ArrayList<>();
        int c = 1;
        for(List<Double> core : cores){
//            double simVal = calSimilarity(pre_plot_RSSI,core);
            double simVal = calSimilarity(location,core);
            System.out.println("与第"+c+"个质心的余弦距离是simval:"+simVal);
            c++;
            cosDiffWithCore.add(simVal);
        }
        // 返回与定位点相似的质心所属的类
        return cosDiffWithCore.indexOf(Collections.max(cosDiffWithCore));
    }

    /**
     * 返回定位点坐标
     *
     * @param clusterId 待定位点所在类的index
     * @return postion 返回待定位点位置坐标（x,y）
     * */
//    public List<Double> WKNN(List<Integer> pre_plot_RSSI, int clusterId, Map<List<Double>, List<List<Integer>>> grouped){
    public List<Double> WKNN(int clusterId, Map<List<Double>, List<List<Integer>>> grouped){
        // postion存定位点坐标
        List<Double> postion = new ArrayList<>();
        // 存此类的中所有指纹
        List<List<Integer>> allFPsInSameCluster = new ArrayList<List<Integer>>();
        // 存与allFPsInSameCluster中所有指纹的余弦相似度
        List<Double> allCosSim = new ArrayList<>();
        //group.get(clusterID)
        // grouped分好类的所有指纹信息
        //TODO 没想好grouped怎么传
//        System.out.println(grouped.get(clusterId));
        int cnt = 0;
        for(List<Double> core : grouped.keySet()){
            if(cnt==clusterId){
                allFPsInSameCluster.addAll(grouped.get(core));
            }
            cnt++;
        }
//        for(int i = 0;i < grouped.get(clusterId).size(); i++){
//            allFPsInSameCluster.add(grouped.get(clusterId).get(i));
//        }
        for (List<Integer> fp : allFPsInSameCluster) {
            // sim是该定位点与allFPsInSameCluster中一个指纹的余弦相似度
            double sim = calReferFPSimilarity(location,fp);
            allCosSim.add(sim);
        }
        // 从allCosSim选取前k个最大的参考点 对allCosSim降序排列
        int k = 4;
//        copyOfAllCosSim 浅拷贝，不能影响原list
        List<Double> copyOfAllCosSim = new ArrayList<>(allCosSim);
        Collections.reverse(copyOfAllCosSim);
        System.out.println("=========after reverse==========");
        List<Integer> index = new ArrayList<>();
        double sumOfCosSim = 0;
        for(int i = 0;i < k;i++){
            System.out.println("第"+i+"个余弦相似度是："+copyOfAllCosSim.get(i)+"；index是"+allCosSim.indexOf(copyOfAllCosSim.get(i)));
            index.add(allCosSim.indexOf(copyOfAllCosSim.get(i)));
            sumOfCosSim += copyOfAllCosSim.get(i);
        }
        System.out.println("sumOfCosSim:"+sumOfCosSim);
        // cost存每个参考点的权值，共k个
        List<Double> cost = new ArrayList<>();
        for(int i = 0;i < k;i++){
            System.out.println("第"+i+"个参考点的权值是"+allCosSim.get(index.get(i))/sumOfCosSim);
            cost.add(allCosSim.get(index.get(i))/sumOfCosSim);
        }
        // 计算坐标
        double x=0;
        double y=0;
        for(int i = 0;i < k;i++){
            x += allFPsInSameCluster.get(index.get(i)).get(0) * cost.get(i);
            y += allFPsInSameCluster.get(index.get(i)).get(1) * cost.get(i);
        }
        postion.add(x);
        postion.add(y);
        System.out.println("定位点的坐标是：（" + x + "," + y + ")");
        return postion;
    }
}
