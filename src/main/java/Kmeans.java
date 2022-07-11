import java.sql.*;
import java.util.*;

public class Kmeans {
    // 源数据
//    private List<Integer> origins = new ArrayList<>();
    private List<List<Integer>> origins = new ArrayList<List<Integer>>();

    // 分组数据
//    private Map<Double, List<Integer>> grouped;
    private Map<List<Double>, List<List<Integer>>> grouped;

    // 初始质心列表
//    private List<Double> cores;
    private List<List<Double>> cores;

    // 数据源
    private String tableName;
//    private String colName;

    /**
     * 构造方法
     *
     * @param tableName 源数据表名称
     * @param cores     质心列表
     */
    private Kmeans(String tableName, List<List<Double>> cores) {
        this.cores = cores;
        this.tableName = tableName;
//        this.colName = colName;
    }

    /**
     * 重新计算质心
     *
     * @return 新的质心列表
     */
    private List<List<Double>> newCores() {
        List<List<Double>> newCores = new ArrayList<List<Double>>();
//        List<Integer> sum = new ArrayList<Integer>();
//        List<Double> avg = new ArrayList<>();

// 新的质心只计算RSSI，改变grouped.values
        int c = 0;
        //grouped.values()是质心，也就是要分为多少类；vv：一个质心
        for (List<List<Integer>> vv : grouped.values()) {
            // 列作外循环，数据库有8列,从坐标x算到ss5
            int j = 0;
            List<Double> tempCore = new ArrayList<>();
            while (j < 7) {
                double temp = 0;
                int i = 0;
                //vv.size()是行数
                for (i = 0; i < vv.size(); i++) {
//                    只求RSSI的平均值，按平均值排序向量
                    temp += vv.get(i).get(j);
                }
                //第c个簇类的质心的向量
                tempCore.add(temp / i);
//                newCores.get(c).add(temp / i);
                j++;
            }
            newCores.add(tempCore);
            c++;
        }

        return newCores;
    }

    /**
     * 判断是否结束
     * 第i簇的core
     *
     * @return bool
     */
    private Boolean isOver() {
        List<List<Double>> _cores = newCores();
        for (int i = 0, len = cores.size(); i < len; i++) {
            for (int j = 0, l = cores.get(j).size(); j < l; j++) {
                if (!cores.get(i).get(j).toString().equals(_cores.get(i).get(j).toString())) {
                    // 使用新质心
                    cores = _cores;
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 数据分组
     * 分组要保留指纹向量所有数据
     */
    private void setGrouped() {
        grouped = new HashMap<>();
        // 返回一个质心：找到该指纹所在类
        List<Double> core;
        for (List<Integer> origin : origins) {
            core = getCore(origin);

            if (!grouped.containsKey(core)) {
                grouped.put(core, new ArrayList<>());
            }
// core标识grouped中的一类，指纹origin属于此类
            grouped.get(core).add(origin);
        }
    }

    /**
     * 选择质心
     *
     * @param num 要分组的数据
     * @return 质心
     */
    private List<Double> getCore(List<Integer> num) {

        // 差 列表
        List<Double> diffs = new ArrayList<>();
        for (List<Double> core : cores) {
            double euclideanDistance = 0;
//           算多维向量（该指纹与每个core）的欧氏距离
            for (int i = 0; i < core.size(); i++) {
                euclideanDistance += Math.sqrt(Math.pow((num.get(i) - core.get(i)),2));
            }
            diffs.add(euclideanDistance);
        }

        // 最小差 -> 索引 -> 对应的质心
        // 第m个指纹所对应的类/质心
//        return cores.get(diffs.get(m).indexOf(Collections.min(diffs)));
        return cores.get(diffs.indexOf(Collections.min(diffs)));
    }

    /**
     * 建立数据库连接
     *
     * @return connection
     */
    private Connection getConn() {
        try {
            // URL指向要访问的数据库名wifiMuseum
            String url = "jdbc:mysql://localhost:3306/wifiMuseum";
            // MySQL配置时的用户名
            String user = "root";
            // MySQL配置时的密码
            String password = "cqueen~cx";
            // 加载驱动
//            Class.forName("com.mysql.jdbc.Driver");
            Class.forName("com.mysql.cj.jdbc.Driver");
            //声明Connection对象
            Connection conn = DriverManager.getConnection(url, user, password);

            if (conn.isClosed()) {
                System.out.println("连接数据库失败!");
                return null;
            }
            System.out.println("连接数据库成功!");
            return conn;
        } catch (Exception e) {
            System.out.println("连接数据库失败！");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 关闭数据库连接
     *
     * @param conn 连接
     */
    private void close(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取源数据
     */
    private void getOrigins() {

        Connection conn = null;
        try {
            conn = getConn();
            if (conn == null) return;
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(String.format("select * from %s", tableName));
            while (rs.next()) {
                List<Integer> ap = new ArrayList<>();
//                origins.add(rs.getInt(1));
//               没取id，从列2开始，也就是从positionx开始
                for (int i = 2; i < 9; i++) {
                    ap.add(rs.getInt(i));
                }
                origins.add(ap);
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(conn);
        }
    }

    /**
     * 处理数据
     */
    private Map<List<Double>, List<List<Integer>>> run() {
        System.out.println("获取源数据");
        // 获取源数据
        getOrigins();

        // 停止分组
        Boolean isOver = false;

        System.out.println("数据分组处理");
        while (!isOver) {
            // 数据分组
            setGrouped();
            // 判断是否停止分组
            isOver = isOver();
        }
        //直接输出
        System.out.println("=====================kmeans=========================");
        for (Map.Entry<List<Double>, List<List<Integer>>> entry : grouped.entrySet()) {
            List<Double> core = entry.getKey();
            System.out.println("这个类的质心是");
//            System.out.println(core.toString());
            System.out.println(core);
            for (List<Integer> value : entry.getValue()) {
                System.out.println("同一类中的AP");
                System.out.println(value.toString());
            }
        }
        return grouped;
    }

    public static void main(String[] args) {
//Kmeans 首先随机指定k个质心，core是list<double>类型，需要手动指定
        List<List<Double>> cores = new ArrayList<List<Double>>();
        List<Double> core1 = new ArrayList<>();
        List<Double> core2 = new ArrayList<>();
//TODO 多个core，将向量以数组形式转为list更简洁
        core1.add(1.0);
        core1.add(1.0);
        core1.add(-56.0);
        core1.add(-59.0);
        core1.add(-85.0);
        core1.add(-100.0);
        core1.add(-100.0);
        core2.add(22.0);
        core2.add(40.0);
        core2.add(-64.0);
        core2.add(-100.0);
        core2.add(-66.0);
        core2.add(-70.0);
        core2.add(-81.0);
        cores.add(core1);
        cores.add(core2);
        // 表名, 列名, 质心列表
//        new Kmeans("fingerprint", cores).run();//"attr_length"
        Location p = new Location(-64,-100,-67,-100,-80);
        p.init();
        p.WKNN(p.getSimilarityGroup(cores),new Kmeans("fingerprint", cores).run());
    }
}
