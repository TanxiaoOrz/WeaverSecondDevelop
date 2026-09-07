const {WeaBrowser} = ecCom;
const {Carousel} = antd;
const isRemind = false

class OverTimeFlag extends React.Component {
    constructor(props) { //初始化，固定语法
        super(props);
        this.state = {}
    }
    render (){
        return (
            <span id="overTime"> 超时 </span>
        )
    }
}

class FinishRemindFlag extends React.Component {
    constructor(props) { //初始化，固定语法
        super(props);
        this.state = {}
    }
    render (){
        return (
            <span id="finishRemind"> 完成 </span>
        )
    }
}

class RequestLogImg extends React.Component {
    constructor(props) { //初始化，固定语法
        super(props);
        this.state = {}
    }
    render (){
        return (
            <img title="查看流转意见" data-requestid={props.requestid} class="portal_workflow_signaturesIcon " src="/images/ecology8/homepage/req_wev8.png" width="14px" height="16px" alt="" />
        )
    }
}


class NewWeaBrowserCom extends React.Component {
    constructor(props) { //初始化，固定语法
        super(props);
        this.state = {}
    }
    render() {
        let newProps = {...this.props};
        //复写组件的时候，必须带上_noOverwrite参数，避免被复写的组件又被复写导致死循环


        let contractRemind = []
        if (isRemind)
            jQuery.ajax({
                url: "/csj/portal/GetHomeTodoCounts.jsp",
                type: 'GET',
                async: false,
                dataType: "json",
                success: function (res) {
                    res.forEach(item => {
                        // 每条提醒占一个待办位置
                        if (newProps.dataSource.length == 8 )
                            newProps.dataSource.pop()

                        contractRemind.push(
                            <li >
                                <span dangerouslySetInnerHTML={{ __html: item.viewString }} style={{fontWeight: 'bold'}}   onClick={()=>window.open(item.href)} ></span>
                                <span><a href={`javascript:openhrm(1)`} onClick={pointerXY.bind(this)}>{"系统管理员"}</a>{<font>{"2026-03-01"}</font>}{<OverTimeFlag />}</span>
                            </li>
                        )
                    })
                }
            });


        var arr = [];
        var listArr = [];
        var pageCount = 8
        console.log("todo",newProps.dataSource)
        newProps.dataSource.map((v,k)=>{
            if(k!=0 && (k+1)%8==0){
                arr.push(newProps.dataSource[k])
                listArr.push({data:arr})
                arr = []
            }else{
                arr.push(newProps.dataSource[k])
                if(newProps.dataSource.length==k+1){
                    listArr.push({data:arr})
                }
            }
        })



        return (
            <div className='ProgressCenter'>

                {
                    listArr && listArr.map((v,k)=>{
                        return (
                            <ul className='flowCenter'>
                                {contractRemind}
                                {
                                    v.data.map((x,y)=>{
                                        return(
                                            <li >
                                                <span dangerouslySetInnerHTML={{ __html: x.requestname.name }} style={{fontWeight: 'bold'}}   onClick={()=>window.open(x.requestname.link)} ></span>
                                                <span><a href={`javascript:openhrm(${x.creater.userid})`} onClick={pointerXY.bind(this)}>{x.creater.name}</a>{newProps.showTime?<font>{(x.createdate?x.createdate:(x.receivedate?x.receivedate:x.operatedate))}</font>:""}{x.requestname.isOverTime==1?<OverTimeFlag />:""}{x.requestname.isDaiYue==1?<FinishRemindFlag />:""}</span>
                                            </li>
                                        )
                                    })
                                }
                            </ul>
                        )
                    })
                }

            </div>
        )
    }
}
//发布模块
ecodeSDK.setCom('${appId}','NewWeaBrowserCom',NewWeaBrowserCom);

