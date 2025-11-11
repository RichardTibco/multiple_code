import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class JsonUrlService {

    // 模拟调用url的方法（耗时1分钟）
    private void callUrl(String url) {
        try {
            System.out.println("开始调用：" + url + "（线程：" + Thread.currentThread().getName() + "）");
            // 模拟1分钟耗时操作
            TimeUnit.MINUTES.sleep(1);
            System.out.println("完成调用：" + url);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("调用被中断：" + url);
        }
    }

    // 解析JSON并并行处理nodes中的url
    public void processJson(String jsonStr) throws Exception {
        // 1. 解析JSON为实体类
        ObjectMapper objectMapper = new ObjectMapper();
        JsonData jsonData = objectMapper.readValue(jsonStr, JsonData.class);
        List<JsonData.Node> nodes = jsonData.getNodes();
        if (nodes == null || nodes.isEmpty()) {
            System.out.println("没有需要处理的节点");
            return;
        }

        // 2. 创建线程池（核心线程数=节点数，避免线程过多）
        ExecutorService executor = Executors.newFixedThreadPool(nodes.size());
        // 用于等待所有任务完成的计数器
        CountDownLatch latch = new CountDownLatch(nodes.size());

        // 3. 提交所有url调用任务到线程池
        long startTime = System.currentTimeMillis();
        for (JsonData.Node node : nodes) {
            String url = node.getUrl();
            executor.submit(() -> {
                try {
                    callUrl(url); // 并行执行调用
                } finally {
                    latch.countDown(); // 任务完成后计数器减1
                }
            });
        }

        // 4. 等待所有任务完成（最多等待2分钟，防止无限阻塞）
        latch.await(2, TimeUnit.MINUTES);
        long endTime = System.currentTimeMillis();

        // 5. 关闭线程池
        executor.shutdown();

        // 6. 输出总耗时
        System.out.println("所有调用完成，总耗时：" + (endTime - startTime) / 1000 + "秒");
    }

    public static void main(String[] args) throws Exception {
        // 测试用的JSON字符串
        String json = "{\n" +
                "    \"name\": \"Json.CN\",\n" +
                "    \"url\": \"http://www.json.cn\",\n" +
                "    \"page\": 88,\n" +
                "    \"isNonProfit\": true,\n" +
                "    \"address\": {\n" +
                "        \"street\": \"科技园路.\",\n" +
                "        \"city\": \"江苏苏州\",\n" +
                "        \"country\": \"中国\"\n" +
                "    },\n" +
                "    \"nodes\": [\n" +
                "        {\n" +
                "            \"name\": \"Google\",\n" +
                "            \"url\": \"http://www.google.com\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"name\": \"Baidu\",\n" +
                "            \"url\": \"http://www.baidu.com\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"name\": \"SoSo\",\n" +
                "            \"url\": \"http://www.SoSo.com\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        new JsonUrlService().processJson(json);
    }
}
