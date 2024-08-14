
jQuery(document).ready(function(){
    const item_discount_authority_field_detail = WfForm.convertFieldNameToId('phzklj','detail_1',true)
    const item_number_field = WfForm.convertFieldNameToId("phwb","detail_1",true)
    let before = window.checkCustomize;
    let block = false;
    function afterCheck(){
        let returns
        let alertStatement = ""
        if (before !== undefined && before!= null)
            returns = before();
        else
            returns = true;


        let rowArr = WfForm.getDetailAllRowIndexStr("detail_1").split(",");
        let index
        let str = ""
        for(let i=0; i<rowArr.length; i++){
            let rowIndex = rowArr[i];
            index = rowIndex;
            if(rowIndex !== ""){
                if (WfForm.getFieldValue(item_discount_authority_field_detail+'_'+rowIndex) == 1) {
                    block = true
                    alertStatement += "品号为"+WfForm.getFieldValue(item_number_field+'_'+rowIndex)+"的产品低于PE特价，请修改后重新提交，谢谢！\n"
                }
            }
        }


        if (block && alertStatement != "") {
            alert(alertStatement)
            return false
        } else
            return returns

    }
    window.checkCustomize = afterCheck;
    console.log("品号最低折扣检查start")
})