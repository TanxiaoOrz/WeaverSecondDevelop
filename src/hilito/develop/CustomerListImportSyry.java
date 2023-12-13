package hilito.develop;

import java.util.HashMap;
import java.util.Map;

import weaver.conn.RecordSet;
import weaver.file.ExcelParseForPOI;
import weaver.formmode.interfaces.ImportFieldTransActionPOI;
import weaver.general.Util;
import weaver.hrm.User;

/**
 * @Author: 张骏山
 * @Date: 2023/12/8 17:16
 * @PackageName: weaver.develop.mode
 * @ClassName: ModeExpandUpdateTerroty
 * @Description: 批量导入时增加使用人员字段
 * @Version: 1.0
 **/
public class CustomerListImportSyry implements ImportFieldTransActionPOI {

    //update uf_customerlist  set uf_customerlist.syry =CUS_FIELDDATA.id from uf_customerlist inner join CUS_FIELDDATA on uf_customerlist.Territory = CUS_FIELDDATA.field0
    // 更新语句
    // update uf_customerlist  set uf_customerlist.syry =vw_Territory.id from uf_customerlist left join vw_Territory on uf_customerlist.Territory = vw_Territory.field0

    private final int TERRITORY_COL = 3;

    @Override
    public String getTransValue(Map<String, Object> param, User user, ExcelParseForPOI excelParse, int row, int col) {
        String fieldid = Util.null2String(param.get("fieldid"));
        //当前字段名(明细表字段名为 d明细表顺序_明细表字段名 如  d1_mx1wb )
        String fieldname = Util.null2String(param.get("fieldname"));
        String sheetindex = Util.null2String(param.get("sheetindex"));
        if (fieldname.equals("syry")) {
            RecordSet recordSet = new RecordSet();
            String territory =  excelParse.getValue(sheetindex, row, TERRITORY_COL);
            HilitoLog.log("开始添加第"+row+"行数据,territory="+territory);
            String sql = "SELECT id FROM CUS_FIELDDATA WHERE CUS_FIELDDATA.field0 = " + territory;
            HilitoLog.log("执行sql="+sql);
            recordSet.execute("SELECT id FROM CUS_FIELDDATA WHERE CUS_FIELDDATA.field0 = " + territory);
            String value;
            if (recordSet.next()) {
                value = Util.null2String(recordSet.getString(1));
            } else
                value = "";
            HilitoLog.log("返回结果="+value);
            return value;
        } else {
            return excelParse.getValue(sheetindex, row, col);
        }
    }

}