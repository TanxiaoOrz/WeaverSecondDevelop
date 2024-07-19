/**
 *
 */
jQuery(document).ready(function(){
    const discount_authority_field_detail = WfForm.convertFieldNameToId('zdzklj','detail_1',true)
    const item_number_field = WfForm.convertFieldNameToId("ph","detail_1",true)
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
                if (WfForm.getFieldValue(discount_authority_field_detail+'_'+rowIndex) == 1) {
                    block = true
                    alertStatement += "品号为"+WfForm.getFieldValue(item_number_field+'_'+rowIndex)+"的产品低于PE特价，请修改后重新提交，谢谢！\n"
                }
            }
        }


        if (block) {
            alert(alertStatement)
            return false
        } else
            return returns

    }
    window.checkCustomize = afterCheck;
    console.log("最低折扣检查start")
})