const config = ecodeSDK.getCom('${appId}', 'config');
console.log(config.base[0].nodeid);

let nodeNumber = -1;
const nodeIdDatas = [
  {
    nodeid:56,
    htbh:"docno",
    bz:"pmdl015",
    zzs:"pmdl012",
    gys:"pmdl004_desc",
    htje:"pmdl041",
    xmh:"xmh",
    xmjl:"pmdlua008_id",
    erpfb:"pmdlsite_desc"
  }
];

let getnodeData = (nodeid)=>{
  // console.log(nodeid==56)
  for (var i = 0;i < nodeIdDatas.length;i++) {
    if (nodeIdDatas[i].nodeid == nodeid)
      return i;
  }
  return -1;
};

ecodeSDK.overwritePropsFnQueueMapSet('WeaReqTop',{ //组件名
    fn:(newProps)=>{ //newProps代表组件参数

    console.log("ecode进入合同金额分配");
    const {Button} = antd;
    const {hash} = window.location;

    if(hash.indexOf('#/main/workflow/req')>-1){
      if(!ecCom.WeaTools.Base64) return newProps; //完整组件库加载完成
      if(!WfForm) return newProps; //表单sdk加载完成
      const baseInfo = WfForm.getBaseInfo();
      console.log(baseInfo);
      // const base = config.base;
      // if (!base[baseInfo.nodeid]) return;
      const {nodeid} = baseInfo;
      if (nodeid==undefined) return;
      console.log(nodeid);
      nodeNumber = getnodeData(nodeid);
      console.log("nodeNumber == "+nodeNumber);
      if (nodeNumber != -1){
        newProps.buttons.push(
          <span>
            <Button type="primary" onClick={()=>{
              var url = "/spa/cube/index.html#/main/cube/card?type=1&modeId=12&formId=-13";
              const {requestid} = WfForm.getBaseInfo();
              var jbbh = 0;
              var sxzt = 0;
              var htbh = WfForm.getFieldValue(WfForm.convertFieldNameToId(nodeIdDatas[nodeNumber].htbh));
              var bz = WfForm.getFieldValue(WfForm.convertFieldNameToId(nodeIdDatas[nodeNumber].bz));
              var zzs = WfForm.getFieldValue(WfForm.convertFieldNameToId(nodeIdDatas[nodeNumber].zzs));
              var gys = WfForm.getFieldValue(WfForm.convertFieldNameToId(nodeIdDatas[nodeNumber].gys));
              var htje = WfForm.getFieldValue(WfForm.convertFieldNameToId(nodeIdDatas[nodeNumber].htje));
              var xmh = WfForm.getFieldValue(WfForm.convertFieldNameToId(nodeIdDatas[nodeNumber].xmh));
              var xmjl = WfForm.getFieldValue(WfForm.convertFieldNameToId(nodeIdDatas[nodeNumber].xmjl));
              var erpfb = WfForm.getFieldValue(WfForm.convertFieldNameToId(nodeIdDatas[nodeNumber].erpfb));
              url += ("&field6937=" + requestid );
              url += ("&field6993=" + jbbh );
              url += ("&field6938=" + bz );
              url += ("&field6949=" + zzs );
              url += ("&field6943=" + gys );
              url += ("&field6930=" + htje );
              url += ("&field6941=" + htbh );
              url += ("&field6994=" + sxzt);
              url += ("&field6933=" + xmh);
              url += ("&field7105=" + xmjl);
              url += ("&field6934=" + erpfb);
              console.log(url);
              window.open(url);
              console.log(1);
            }}>分配合同金额</Button>
          </span>
        ); 

      }
      return newProps;
    }
    },
    order:1, //排序字段，如果存在同一个页面复写了同一个组件，控制顺序时使用
    desc:'在这里写此复写的作用，在调试的时候方便查找'
});