  //alert(1);
  function formpost(paramUrl) { 
    const url = "/zrJsp/login.jsp"
  	var tempform = document.createElement("form");      
    tempform.action = url;      
    tempform.target = "_blank";
    tempform.method = "post";      
    tempform.style.display = "none"; 
    var opt = document.createElement("textarea");      
		opt.name = "url";      
    opt.value = paramUrl;      
    tempform.appendChild(opt); 
    document.body.appendChild(tempform);
    tempform.submit();
  }

  var rowArr = WfForm.getDetailAllRowIndexStr("detail_1").split(",");
  for(var i=0; i<rowArr.length; i++){
    var rowIndex = rowArr[i];
    if(rowIndex !== ""){
        let czgn = WfForm.convertFieldNameToId("schemaSupplierUrl","detail_1")+"_"+rowIndex;
        let czgnUrl = WfForm.getFieldValue(czgn);    //遍历明细行字段
        console.log("url="+czgnUrl)
        console.log("fieldid="+czgn)
        //WfForm.changeFieldValue(czgn, {value:null}); 
        WfForm.proxyFieldComp(czgn,React.createElement("button",{
          type:"button",
          class:"ant-btn ant-btn-primary2",
          id:"btn"+rowIndex,
          title:"跳转",
          children:"查看报价",
          margin: "5px",
          onClick:function(){
            const url = czgnUrl+"&loginKey=";
            //alert(url);
            formpost(url);
          }
        }));
      }
  }