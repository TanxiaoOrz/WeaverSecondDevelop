let enable = true;
let isRun = false; //控制执行次数
let con = false;
let confirm = false;
const nodes = [56,0];

//PC端代码块
//利用组件复写作为代码块执行钩子，这种方案可以支持覆盖到所有流程，也可以判断到指定流程指定节点

ecodeSDK.overwritePropsFnQueueMapSet('WeaReqTop',{
  fn:(newProps)=> {
    if(!enable) return ; //开关打开
    const {hash} = window.location;
    if(!hash.startsWith('#/main/workflow/req')) return ; //判断页面地址
    if(!ecCom.WeaTools.Base64) return ; //完整组件库加载完成
    if(!WfForm) return ; //表单sdk加载完成
    if(isRun) return ; //执行过一次就不执行
    const baseInfo = WfForm.getBaseInfo();
    const {workflowid} = baseInfo;
    const {nodeid} = baseInfo;
    console.log(nodeid);
    console.log(!nodes.includes(nodeid));
    //if(workflowid!==15) return ; //判断流程id
    if(!nodes.includes(nodeid)) return ; //判断节点id
    //alert(1);
    console.log("distrubuteCheckOpen")
    window.checkCustomize = ()=>{ //代码块钩子，类似放在代码块中或者jquery.ready
      //可操作WfForm，以及部分表单dom hiden、ReactDOM.render
      isRun = true;
      if (confirm) 
        return true
      //alert(1);
      const {requestid} = baseInfo
      const {formid} = baseInfo
      let url ="/zrJsp/judgeAllMoneyDistributed.jsp?requestid="+requestid + "&formid=" +formid;
      //alert(url);
      jQuery.ajax({
        url: url,
        type: "get",
        async: false,
        cache: false,
        success: function(msg){
          //alert("已更新已读");
          console.log(msg);
          con = parseInt(msg);
        },
        failure: function(msg){
          // console.log(msg);
        }
      });
      // console.log(con);
      if (con!=-1) {
        if (con == 0)
          return true;
        else {
          //alert("金额分配超出预算，增加审批流程");
          WfForm.showConfirm("金额分配超出预算，增加审批流程",function(){
              confirm = true;
              WfForm.changeFieldValue("field7225", {value:1});
              WfForm.triggerFieldAllLinkage("field7226");
              WfForm.doRightBtnEvent("BTN_SUBMIT");
          },function(){
              //alert("点击取消调用的事件");
          },{
              title:"金额控制提醒",       //弹确认框的title，仅PC端有效
              okText:"确认",          //自定义确认按钮名称
              cancelText:"重新分配"     //自定义取消按钮名称
          });
          return false;
        }
      } else {
        alert("未进行金额分配，不允许提交");
        return false;
      }
    }
    isRun = true;
  }
});
