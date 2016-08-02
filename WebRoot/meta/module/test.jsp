<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%--<%@ include file="../../headConfig.jsp" %>--%>
<html>
<title>用户管理</title>
<head>
<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
<meta http-equiv="description" content="this is my page">
<meta http-equiv="content-type" content="text/html; charset=UTF-8">
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/meta/resource/css/icon.css">
<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/meta/resource/css/validation.css">
<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/meta/resource/dhtmlx/dhtmlx.css">
<script type='text/javascript' src='${pageContext.request.contextPath}/dwr/engine.js'></script>

<script type='text/javascript' src='${pageContext.request.contextPath}/dwr/util.js'></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/meta/resource/dhtmlx/dhtmlx.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/meta/resource/js/dhtmlxgrid_filter.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/meta/resource/js/dhtmlxcombo.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/meta/resource/js/datastore_debug.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/meta/resource/js/dhtmlxgrid_pgn.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/meta/resource/js/dhtmlxgrid_json.js"></script>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/meta/resource/js/dhtmlxdataprocessor.js"></script>
<%--<script type="text/javascript"
        src="${pageContext.request.contextPath}/meta/resource/js/dhtmlxdataprocessor_debug.js"></script>--%>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/meta/resource/js/dhtmlxdataprocessor_deprecated.js"></script>
<script type="text/javascript"
        src="${pageContext.request.contextPath}/meta/resource/js/dhtmlxgrid_validation.js"></script>
<%--<script type="text/javascript"--%>
<%--src="${pageContext.request.contextPath}/meta/resource/js/dhtmlxform.js"></script>--%>
<script type="text/javascript" src="${pageContext.request.contextPath}/meta/resource/js/dhtmlxExtend.js"></script>
<script type='text/javascript' src='${pageContext.request.contextPath}/dwr/interface/DhtmlxAction.js'></script>

<script type="text/javascript">
    var getSkin = function(){
        //TODO 根据session中的skin配置获取用户的皮肤设置。
        return "dhx_skyblue";
    }
    var getDefaultImagePath = function(){
        return "${pageContext.request.contextPath}/meta/resource/dhtmlx/imgs/";
    }
</script>
<%--<bi:dwr dwrCtrls="DhtmlxAction"/> &lt;%&ndash;此标签代替dwr文件引入&ndash;%&gt;--%>
<script type="text/javascript">
window.onload = function(){
    var formater = function(value, rowData, rowIndex){
        return "转义";
    }

    /* * 示例1：利用biDwrCaller控件用直接加载数据*/
    /*var grid=  dhtmlXGridObject.instance('user');
     //bidwrCaller 两种使用方式
     var dwr=new biDwrCaller({
     loadBySelf:function(afterCall,param){ //完全自主控制
     //直接调用Dwr
     DhtmlxAction.dhtmlGridData(null,param,function(data){
     if(data){
     afterCall(data);//调用afterCall解析
     }
     })
     },
     loadByAuto:{methodName:"DhtmlxAction.dhtmlGridData",
     param:[null],//分页参数控件自动处理
     success:function(data){//自定义回调可以做数据处理
     return data;
     }
     }
     });
     grid.defualtPaging(15); //分页
     grid.showRowNumber(); //显示行号
     //    grid.load(dwr.loadBySelf,"json");//用自定义方式
     grid.load(dwr.loadByAuto,"json");//用自动加载方式
     */
    /* 示例2:利用dataStore用biDwrCaller控件加载table数据*/

    /*  var grid=  dhtmlXGridObject.instance('user');
     var dwr=new biDwrCaller();
     dwr.loadAction("DhtmlxAction.dhtmlXuserList",null);//执行Dwr方法 DhtmlxAction.dhtmlXuserList
     grid.defualtPaging(15); //分页
     grid.showRowNumber(); //显示行号

     var data = new dhtmlXDataStore({
     url: dwr.load,
     datatype: "json",
     pageSize:15 //每页显示行数,如果不设置会在第一次加载的时候加载所有的数据
     });
     grid.sync(data);//数据同步。
     */



    /*
     示例3:利用dataStore用biDwrCaller控件加载table数据，并且进行数据处理。
     */
    /*var grid=  dhtmlXGridObject.instance('user');
     var dwr=new biDwrCaller();
     dwr.loadAction("DhtmlxAction.dhtmlGridData",null,function(data,afterCall){//回调函数显示进行数据处理,将其解析成dataStore符合的格式
     var totalcount=data.total_count
     var pos=data.pos;
     var rows=data.rows;
     var result=new Object();
     result.total_count=totalcount;
     result.pos=pos;
     var list=[];
     for(var i=0;i<rows.length;i++){
     var row=[];
     row.id=rows[i].id;
     for(var j=0;j<rows[i].data.length;j++){
     row["cell"+j]=rows[i].data[j];
     }
     list.push(row);
     }
     result.data=list;
     return result;//显示返回处理后的数据，即可进行下面的解析动作，如果不需要grid自动解析，返回false，有时候需要等待其他动作完成
     //才能进行解析动作，此时可以先让程序返回false,然后显示调用afterCall函数进行数据加载动作。
     });//执行Dwr方法 DhtmlxAction.dhtmlXuserList
     grid.defualtPaging(15); //分页
     grid.showRowNumber(); //显示行号
     var data = new dhtmlXDataStore({
     url: dwr.load,
     datatype: "json",
     pageSize:15 //每页显示行数,如果不设置会在第一次加载的时候加载所有的数据
     });
     grid.sync(data);//数据同步。*/

    /*
     示例4,利用dataStore进行grid和combo数据的联动
     */

    /*  var grid=  dhtmlXGridObject.instance('user');
     var dwr=new biDwrCaller();
     dwr.loadAction("DhtmlxAction.dhtmlXuserList",null);//执行Dwr方法 DhtmlxAction.dhtmlXuserList
     grid.defualtPaging(100); //分页
     grid.showRowNumber(); //显示行号
     var data = new dhtmlXDataStore({
     url: dwr.load,
     datatype: "json"
     });
     grid.sync(data);//数据同步。

     //combo option设置
     data.data.scheme({
     $init: function(obj) {
     obj.value = obj.id;
     obj.text = obj.cell1;
     }
     });
     //            grid.enableSmartRendering(true);
     dhtmlx.image_path = "${pageContext.request.contextPath}/resource/dhtmlx/imgs/";
     dhtmlx.skin = "dhx_skyblue";
     var combo1 = new dhtmlXCombo("combo_here1", "combo1", 200);
     combo1.sync(data); //数据同步

     //grid根据规则绑定到combo上
     grid.bind(combo1, function(data, filter) {
     return grid.cells(data, 0).getValue() == filter.value;
     });*/


    /*
     示例5:两个combo利用两个dataStore进行联动
     */
    /* var dwr=new biDwrCaller();
     dwr.loadAction("DhtmlxAction.dhtmlXuserList",null);
     var list1 = new dhtmlXDataStore({
     url: dwr.load,
     datatype: "json"
     });
     dwr=new biDwrCaller();
     dwr.loadAction("DhtmlxAction.dhtmlXuserList",null);
     list1.data.scheme({
     $init: function(obj) {
     obj.value = obj.id;
     obj.text = obj.cell1;
     }
     });
     var list2 = new dhtmlXDataStore({
     url: dwr,
     datatype: "json"
     });
     list2.data.scheme({
     $init: function(obj) {
     obj.value = obj.id;
     obj.text = obj.cell2;
     }
     });
     //绑定
     list2.bind(list1, function(data, filter) {
     return data.value == filter.value;
     });

     dhtmlx.image_path = "${pageContext.request.contextPath}/resource/dhtmlx/imgs/";
     dhtmlx.skin = "dhx_skyblue";
     var combo1 = new dhtmlXCombo("combo_here1", "combo1", 200);
     combo1.sync(list1); //数据同步
     var combo2 = new dhtmlXCombo("combo_here2", "combo2", 200);
     combo2.sync(list2);
     combo1.attachEvent("onSelectChange", function(id) {
     list1.setCursor(id);
     });*/



    /*示例6 自定义控件biDwrMethodParam、biDwrCaller的使用。*/

    /*var dwr=new biDwrCaller();
     dwr.loadAction("DhtmlxAction.dhtmlGridData");//执行Dwr方法 DhtmlxAction.dhtmlGridData
     var testParam= new biDwrMethodParam();
     testParam.setParamConfig([{
     index:0,type:"ui",value:"_serach,id"
     },{
     index:1,type:"fun",value:function(){return {start:0}}
     }]);
     testParam.setParamConfig([{
     index:1,type:"watch",value:[dwr,"doActionload"],watchCallback:function(firstArg,second){
     if(firstArg.length==2){//无分页参数，为第一次访问,下次访问系统会自动传分页参数，去掉分页位的设置
     testParam.clearConfigByIndex(1);
     }
     return null;
     }
     },{
     index:1,type:"static",value:{count:15}
     }
     ]);*/
    /*  biDwrMethodParam的灵活设置*/
//    var dwrtest=new biDwrCaller();
//    dwrtest.loadAction("DhtmlxAction.dhtmlXuserList",testParam);//执行Dwr方法 DhtmlxAction.dhtmlXuserList
//    var count=0;
//    setInterval((function(){
//        testParam.setParam(0,{passwd:"zzzzzz"+count,stationId:count++},false);
//        testParam.setParam(1,{count:15+count},false);
//        dwrtest.doActionload();
//    }),9999);
    // biDwrMethodParam与dwrtest联合作为URL使用

    /* var grid=  dhtmlXGridObject.instance('user');
     grid.defualtPaging(15); //分页
     grid.showRowNumber(); //显示行号
     grid.load(dwr+testParam,"json");//传统方式加载数据。
     */
    /*
     示例7 两个datagrid控件之间的联动
     */
    /*var grid=  dhtmlXGridObject.instance('user');
     var dwr=new biDwrCaller();
     dwr.loadAction("DhtmlxAction.dhtmlGridData",null,null);//执行Dwr方法 DhtmlxAction.dhtmlGridData
     grid.defualtPaging(15); //分页
     grid.showRowNumber(); //显示行号
     grid.load(dwr.load,"json");//传统方式加载数据。
     grid.enableLightMouseNavigation(false);//此选项删除取消鼠标默认选择方式

     var grid2=  dhtmlXGridObject.instance('user2');
     var dwr2=new biDwrCaller();
     dwr2.addAutoAction("filter","filter");//执行Dwr方法 DhtmlxAction.dhtmlGridData
     grid2.defualtPaging(15); //分页
     grid2.showRowNumber(); //显示行号
     grid2.dataFeed(dwr2.filter);//传统方式加载数据。
     grid2.bind(grid,function(data,filter){
     filter.name = data.cell2;
     });*/

    /* 示例8 datagrid和form之间的联动*/
    /* var grid=  dhtmlXGridObject.instance('user');
     var dwr=new biDwrCaller();
     dwr.loadAction("DhtmlxAction.dhtmlGridData",null,null);//执行Dwr方法 DhtmlxAction.dhtmlGridData
     grid.defualtPaging(15); //分页
     grid.showRowNumber(); //显示行号
     grid.load(dwr.load,"json");//传统方式加载数据。
     grid.setColumnIds("Count,Name");

     grid.bind(myform, function(data, master) {
     if (master.Name == "")
     return true;
     return grid.cells(data, 3).getValue().toLowerCase().indexOf(master.Name) != -1;
     });
     */
    /* 示例9:利用biDwrCaller、DataProcessor进行增删改查，验证等*/
    grid = dhtmlXGridObject.instance('user2');
    //bidwrCaller 两种使用方式
    var dwr = new biDwrCaller({
                                  update:function(afterCall, param){ //完全自主控制
                                      //直接调用Dwr
                                      DhtmlxAction.update(param, function(data){
                                          if(data){
                                              afterCall(data);//调用afterCall解析
                                          }
                                      })
                                  } ,
                                  deleted:function(afterCall, param){
                                      //直接调用Dwr
                                      DhtmlxAction.update(param, function(data){
                                          if(data){
                                              afterCall(data);//调用afterCall解析
                                          }
                                      })
                                  },
                                  insert:function(afterCall, param){
                                      //直接调用Dwr
                                      DhtmlxAction.update(param, function(data){
                                          if(data){
                                              afterCall(data);//调用afterCall解析
                                          }
                                      })
                                  },
                                  load:{methodName:"DhtmlxAction.dhtmlGridData",
                                      param:[null],//分页参数控件自动处理
                                      success:function(data){//自定义回调可以做数据处理
                                          return data;
                                      }
                                  },
                                  loadTest:function(after,param){
                                      DhtmlxAction.dhtmlGridData(null,param,{
                                          callback:function(data){
                                              $("formtest").innerHTML+=data;
                                          }
                                          ,async:false});
                                      $("formtest").innerHTML+="yyyy"
                                  },
                                  remote:{//自动验证
                                      methodName:"DhtmlxAction.remote",
                                      success:function(data){//自定义回调可以做数据处理
                                          return data;
                                      }
                                  },
                                  remoteBySelf:function(afterCall,param){
                                      DhtmlxAction.remote(param,{
                                          callback:function(data){
                                              afterCall(data);//记住一定要调用afterCall(data),否则验证会出问题
                                          },
                                          async:false//远程验证一定要同步方式,切记,切记。
                                      })
                                  }


                              });
    grid.defualtPaging(15); //分页
    grid.showRowNumber(); //显示行号
    grid.load(dwr.load, "json");//用自动加载方式
    grid.setColumnIds("cnt,checked,book,author");
    grid.enableValidation(true,true);
    grid.setColValidators(",,Remote["+dwr.remoteBySelf+"],Range[13&18]&EqualTo[id]&NotEmpty");
    grid.defaultValidateEvent();

    dp = new dataProcessor(dwr);
    dp.init(grid);
    dp.setTransactionMode("POST", true);
    dp.enableDataNames(true);
    dp.setUpdateMode("off");

    var formData = [{
        type: "label",
        label: "Filter"
    }, {
        type: "input",
        name: "Name",
        value: "",
        labelWidth: 100,
        label: "Name",
        validate:"Remote["+dwr.remoteBySelf+"],MinLength[1],MaxLength[5],ValidByCallBack[validateBySelf]"
    },{
        type:"button",
        name:"my_submit_button",
        command:"save",
        value:"提交"
    }];
    var myform = new dhtmlXForm("formtest", formData);
    myform.defaultValidateEvent();
    myform.attachEvent("onButtonClick", function(name, command){
        if(name=="my_submit_button"){
            myform.validate();
        }
    });

}
var __submit = function(){
    dp.sendData();
}
var deleteRow = function(){
    var checked = grid.getCheckedRows(1);
    if(checked){
        checked = checked.split(",");
        for(var i = 0; i < checked.length; i++){
            grid.deleteRow(checked[i])
        }
    }
}

var validateBySelf=function(value){
    return value;
}
var insertRow = function(){
    grid.addRow(111, "0,新增1,新增2", 1);
}
function not_empty(value, id, ind){
    if(value == "");
    return "Value at (" + id + ", " + ind + ") can't be empty";
    return true;
}

var showUserDialog = function(param1, param2){
    return "张村" + param2;
}

//    alert(a.rows.length)
</script>
</head>

<body>

<%--新增用户弹出框--%>

<%--<div style="display:none;">--%>
<%--<bi:dialog width="500" height="250" buttons="#new_dlg_buttons"  index="3" title="新增用户" contentUrl="showAddUser.do" />
&lt;%&ndash;</div>&ndash;%&gt;

<bi:dialog width="300" height="140" buttons="#edit_password_dlg_buttons" contentUrl="showPassword.do" index="1" title="重置密码"/>
<bi:dialog width="500" height="250" buttons="#edit_dlg_buttons" contentUrl="userEditIndex.do" index="2" title="编辑用户"/>

<bi:dialog width="1000" height="455"  contentUrl="showUserRefRole.do" index="4" title="关联权限"/>
<bi:dialog width="470" height="455"  contentUrl="showUserRefMenu.do" index="5" title="关联菜单"/>--%>

<%--<bi:dialog width="300" height="150"   index="6" title="选择没有权限的按钮" buttons="#select_but_dlg_buttons">
    <div style=" PADDING-LEFT: 10px; PADDING-RIGHT: 10px; HEIGHT: auto; PADDING-TOP: 20px;bottom:0">
        请选择没有权限的按钮： <select id="_addbutton" name="dept" style="width:100px;height:400px" multiple="multiple" mode="remote">
    </select>
    </div>
    <div id="select_but_dlg_buttons">
        <a href="#" class="easyui-linkbutton" onclick="javascript:addButton();">确定</a>
        <a href="#" class="easyui-linkbutton" onclick="javascript:eval('dialogClass_6').close()">关闭</a>
    </div>
</bi:dialog>--%>
<%--用户列表--%>
<input type="button" value="保存" onclick="javascript :__submit()"/>
<input type="button" value="删除" onclick="javascript :deleteRow()"/>
<input type="button" value="新增" onclick="javascript :insertRow()"/>

<div id="formtest" style="width:600px; height:50px; background-color:white;"></div>

<input type="hidden" value="0" name="id">

<div id="tb" style="padding:5px;height:auto">
    <form id="_serach" action="#" style="padding:0px; margin:0px;">
        <div style="margin-bottom:5px">
            <input type="button" onclick="showUserDialog(this,'newUserbtn')" id="newUserbtn">新增用户</a>
            <a href="#" class="easyui-linkbutton" iconCls="icon-edit" plain="true" onclick="showEditUserDialog()"
               id="editUserbtn">编辑用户</a>
            <%--<a href="#" class="easyui-linkbutton" iconCls="icon-save" plain="true"></a>--%>
            <%--<a href="#" class="easyui-linkbutton" iconCls="icon-cut" plain="true"></a>--%>
            <a href="#" class="easyui-linkbutton" iconCls="icon-remove" plain="true" onclick="removeUser()"
               id="removerUserbtn">删除用户</a>
            <a href="#" class="easyui-linkbutton" iconCls="icon-back" plain="true" onclick="showEditPassDialog()"
               id="modifyUserPassbtn">重置密码</a>
            <a href="#" class="easyui-linkbutton" iconCls="icon-redo" plain="true" onclick="showRefRoleDialog()"
               id="userRefRolebtn">关联角色</a>
            <a href="#" class="easyui-linkbutton" iconCls="icon-redo" plain="true" onclick="showRefMenuDialog()"
               id="userRefMenubtn">关联菜单</a>
        </div>

        <div>
            邮件： <input style="width:150px" name="email" type="text"/>
            姓名: <input style="width:150px" name="nameCN" type="text"/>
            <%--Language:--%>
            <%--<input class="easyui-combobox" style="width:100px"--%>
            <%--url="data/combobox_data.json"--%>
            <%--valueField="id" textField="text">--%>
            <%--<a href="#" class="easyui-linkbutton" iconCls="icon-search">Search</a>--%>
        </div>
        <div>
            手机： <input style="width:150px;background:#006400" name="mobile" type="text"/>
            <%--状态: <input class="easyui-input" style="width:150px" name="nameCN">--%>
            状态:
            <select name="state" style="width:150px">
                <option value="0">未选择</option>
                <option value="1">可用</option>
                <option value="2">不可用</option>
            </select>
            <a href="#" class="easyui-linkbutton" iconCls="icon-search" onclick="buttonSearch()">搜索</a>
        </div>
    </form>
</div>

<%--<bi:datagrid loadMsg="数据正在加载.." url="listUser.do" pagination="true" initfun="init">--%>
<%--<table id="tt" style="width:auto;height:auto;display:inline;" title="用户列表" iconCls="icon-edit" singleSelect="false"--%>
<%--idField="id" toolbar="#tb" fitColumns="true">--%>
<%--<thead>--%>
<%--<tr>--%>
<%--<th field="ck" checkbox="true" ></th>--%>
<%--<th field="id" width="0" align="center" >用户ID</th>--%>
<%--<th field="email" width="150" align="center">邮件</th>--%>
<%--<th field="namecn" width="110" align="center">姓名</th>--%>
<%--<th field="mobile" width="110" align="center" editor="numberbox">手机号码</th>--%>
<%--&lt;%&ndash;<th field="deptid" width="97" editor="text" align="center">部门</th>&ndash;%&gt;--%>
<%--<th field="stationid" width="0" align="center">岗位</th>--%>
<%--<th field="stationname" width="110" align="center">岗位</th>--%>
<%--<th field="state" width="110" editor="text" formatter="stateFormatter" align="center">状态</th>--%>
<%--<th field="magflag" width="110" editor="text" formatter="magFlagFormatter" align="center">管理权限</th>--%>
<%--</tr>--%>
<%--</thead>--%>
<%--</table>--%>
<%--</bi:datagrid>--%>
<div id="combo_here1" style="background-color:white;"></div>
<div id="combo_here2" style="background-color:white;"></div>

<div id="test">
    <%--<table style="height:100%;width:100%;background:aqua">--%>
    <%--<tr>--%>
    <%--<td></td>--%>
    <%--</tr>--%>
    <%--</table>--%>
    <table id="user" style="width:100%;" imgpath="${pageContext.request.contextPath}/meta/resource/dhtmlx/imgs/"
           border="1" lightnavigation="true" autoHeight="true">
        <tr heightStyle="height:30px">
            <%--<td width="12"></td>--%>
            <%--<td  width="20" align="center" type="cntr"></td>--%>
            <td width="30" align="center" type="ch">{#checkBox}</td>
            <td width="*" align="center">邮件</td>
            <td width="20" align="center">姓名</td>
            <%--  <td  width="*" align="center" >手机号码</td>
       &lt;%&ndash;<th field="deptid" width="97" editor="text" align="center">部门</th>&ndash;%&gt;
       <td  width="0" align="center">岗位</td>
       <td  width="*" align="center" >岗位</td>
       <td  width="*"  align="center" customerFormater="stateFormatter">状态</td>
       <td  width="*"  align="center" customerFormater="magFlagFormatter">管理权限</td>--%>
        </tr>
    </table>

    <table id="user2" style="width:100%;" imgpath="${pageContext.request.contextPath}/meta/resource/dhtmlx/imgs/"
           border="1" lightnavigation="true" autoHeight="true">
        <tr heightStyle="height:30px">
            <td width="30" align="center" type="ch">{#checkBox}</td>
            <td width="150" align="center" type="ed">邮件</td>
            <td width="*" align="center" type="ed">姓名</td>
        </tr>
    </table>
</div>


</body>


</html>
