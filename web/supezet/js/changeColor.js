let enable = true;
let isRun = false; //控制执行次数
const renderModified = ()=>{ //代码块钩子，类似放在代码块中或者jquery.ready
  let changePropertyMain = WfForm.getFieldValue(WfForm.convertFieldNameToId("changeProperty"));
  let coloums = changePropertyMain.split(",");
  coloums.forEach(function(coloum) {
      //alert(WfForm.convertFieldNameToId(coloum));
      jQuery("#"+WfForm.convertFieldNameToId(coloum)).css("color","#ff0000");
  })
  let rowArr1 = WfForm.getDetailAllRowIndexStr("detail_1").split(",");
  rowArr1.forEach(function(row) {
    let changePropertyDt1 = WfForm.getFieldValue(WfForm.convertFieldNameToId("changeProperty")+"_"+row,"detail_1");
    let coloumsDt1 = changePropertyDt1.split(",");
    coloumsDt1.forEach(function(coloum) {
      jQuery("#"+WfForm.convertFieldNameToId(coloum)+"_"+row).css("color","#ff0000");
    });
  });
  let rowArr2 = WfForm.getDetailAllRowIndexStr("detail_2").split(",");
  rowArr2.forEach(function(row) {
    let changePropertyDt2 = WfForm.getFieldValue(WfForm.convertFieldNameToId("changeProperty")+"_"+row,"detail_2");
    let coloumsDt2 = changePropertyDt2.split(",");
    coloumsDt2.forEach(function(coloum) {
      jQuery("#"+WfForm.convertFieldNameToId(coloum)+"_"+row).css("color","#ff0000");
    });
  });
  let rowArr3 = WfForm.getDetailAllRowIndexStr("detail_3").split(",");
  rowArr3.forEach(function(row) {
    let changePropertyDt3 = WfForm.getFieldValue(WfForm.convertFieldNameToId("changeProperty")+"_"+row,"detail_3");
    let coloumsDt3 = changePropertyDt3.split(",");
    coloumsDt3.forEach(function(coloum) {
      jQuery("#"+WfForm.convertFieldNameToId(coloum)+"_"+row).css("background-color","#ff0000");
    });
  });
  //isRun = true; //确保只执行一次
  //alert(1);
}
//PC端代码块
//利用组件复写作为代码块执行钩子，这种方案可以支持覆盖到所有流程，也可以判断到指定流程指定节点
ecodeSDK.overwritePropsFnQueueMapSet('WeaReqTop',{
  fn:(newProps)=>{
    if(!enable) return ; //开关打开
    const {hash} = window.location;
    if(!hash.startsWith('#/main/workflow/req')) return ; //判断页面地址
    if(!ecCom.WeaTools.Base64) return ; //完整组件库加载完成
    if(!WfForm) return ; //表单sdk加载完成
    const baseInfo = WfForm.getBaseInfo();
    const {workflowid} = baseInfo;
    if(workflowid!==16) return ; //判断流程id
    if(isRun) return ; //执行过一次就不执行
    renderModified(); //执行代码块
  }
});
