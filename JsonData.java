import lombok.Data;
import java.util.List;

// 根节点实体
@Data
public class JsonData {
    private String name;
    private String url;
    private int page;
    private boolean isNonProfit;
    private Address address;
    private List<Node> nodes;

    // 地址子节点
    @Data
    public static class Address {
        private String street;
        private String city;
        private String country;
    }

    // 节点子节点（包含需要调用的url）
    @Data
    public static class Node {
        private String name;
        private String url;
    }
}
