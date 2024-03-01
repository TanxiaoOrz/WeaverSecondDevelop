window.checkCustomize = ()=>{
  var str = "";
  ModeForm.changeFieldValue("field19953",{value:0});
  let purchaseMoney = ModeForm.getFieldValue("field20065");
  let distributeMoney = ModeForm.getFieldValue("field20020");
  if (purchaseMoney != distributeMoney) {
    str +=("分配金额"+distributeMoney+"不等于采购金额"+purchaseMoney+",不允许提交");
    return false;
  }
  let rowArray = ModeForm.getDetailAllRowIndexStr("detail_1").split(",");
  
  for(let i=0; i<rowArray.length; i++){
    var rowIndex = rowArray[i];
    if(rowIndex != ""){
        var supplierCheck = ModeForm.getFieldValue("field20057_"+rowIndex)
        var name = ModeForm.getFieldValue("field20058_"+rowIndex)
        console.log(askedSupplier);
        if (supplierCheck == 0) {
          alert("该供应商不在"+name+"预算细项供应商名录内，需发起既定预算变更审批</br>");
          return false;
        }
        //alert("gongyings")
        var restBudget =parseFloat(ModeForm.getFieldValue("field20099_"+rowIndex));    //遍历明细行字段
        var overBudget =parseFloat(ModeForm.getFieldValue("field20093_"+rowIndex) * ModeForm.getFieldValue("field20090_"+rowIndex));    //遍历明细行字段
        if (restBudget<0) {
          // alert(restBudget);
          if (restBudget+overBudget>=0) {
            ModeForm.changeFieldValue("field19953",{value:1});
            //alert("预算明细"+name+"超出既定预算，将增加审批环节")
            // 将增加审批环节代码等待添加
          } else {
            alert("预算明项"+name+"超出最大使用上限，不允许提交");
            // alert("overBudget"+overBudget);
            // alert("restBudget"+restBudget)
            // alert("(overBudget+restBudget)"+(overBudget+restBudget))
            return false;
          }
        }
    }
  }
  alert(str)
  return false;
}

function _customAddFun1(addIndexStr){ 
    let purchaseMoney = ModeForm.getFieldValue("field20065");
    let distributeMoney = ModeForm.getFieldValue("field20020");
    let money = ModeForm.getFieldValue("field20096_"+addIndexStr);
    console.log(purchaseMoney- distributeMoney);
    console.log("新增的行标示："+addIndexStr); 
    console.log(money);
    if (money==""||money==null)
      ModeForm.changeFieldValue("field20096_"+addIndexStr, {    
        value: purchaseMoney- distributeMoney});  
}

console.log("启用")
