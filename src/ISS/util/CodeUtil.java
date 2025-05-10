package ISS.util;

import okhttp3.*;
import org.json.JSONObject;

import java.util.Date;

/**
 * @Author: 张骏山
 * @Date: 2025/5/10 16:25
 * @PackageName: ISS.util
 * @ClassName: CodeUtil
 * @Description: e10的code
 * @Version: 1.0
 */
public class CodeUtil {

    private String corpid = ConfigUtil.getE10CorpId();
    private static CodeUtil codeUtil;

    public CodeUtil(String corpid) {
        this.corpid = corpid;
    }

    public CodeUtil() {
    }

    public static CodeUtil getInstance() {
        if (codeUtil == null) {
            codeUtil = new CodeUtil();
        }
        return codeUtil;
    }

    public String getCode() {
        try {
            JSONObject param = new JSONObject();
            param.put("corpid",corpid);
            param.put("response_type","code");
            param.put("state","");
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, param.toString());
            Request request = new Request.Builder()
                    .url(ConfigUtil.getE10Url() + "/papi/openapi/oauth2/authorize")
                    .method("POST", body)
                    .addHeader("Content-Type", "application/json")
                    .build();
            Response response = client.newCall(request).execute();
            JSONObject rtnJson = new JSONObject(response.body().string());
            return rtnJson.getString("code");
        } catch (Exception e) {
            e.printStackTrace();
            Console.log(e.getMessage());
            return "";
        }
    }

    public static void main(String[] args) {
        String code = new CodeUtil().getCode();
        System.out.println("args = " + code);
    }

}
