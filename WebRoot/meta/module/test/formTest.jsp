<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<title>表单操作示例</title>
<head>
    <%--<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/meta/resource/css/icon.css">--%>
    <%--<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/meta/resource/css/validation.css">--%>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/meta/resource/dhtmlx/dhtmlxform_dhx_skyblue.css">
        <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/meta/resource/dhtmlx/style.css">
    <%--<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/meta/resource/css/icon.css">--%>
    <%--<link type="text/css" rel="stylesheet" href="${pageContext.request.contextPath}/meta/resource/css/validation.css">--%>
    <%--<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/meta/resource/dhtmlx/dhtmlx.css">--%>
    <script type='text/javascript' src='${pageContext.request.contextPath}/dwr/engine.js'></script>
    <script type='text/javascript' src='${pageContext.request.contextPath}/dwr/util.js'></script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/meta/resource/dhtmlx/dhtmlx.js"></script>
    <%--<script type="text/javascript" src="${pageContext.request.contextPath}/meta/resource/js/dhtmlxExtend.js"></script>--%>
    <script type="text/javascript" src="${pageContext.request.contextPath}/meta/resource/js/dhtmlx_i18n_zh.js"></script>
    <script type='text/javascript' src='${pageContext.request.contextPath}/dwr/interface/DhtmlxAction.js'></script>
    <%--<script type="text/javascript" src="${pageContext.request.contextPath}/meta/resource/js/dhtmlxform.js"></script>--%>
</head>
<body>
<!--示例一：表单数据加载。-->

<script>
    var myForm,  formData;
    var doOnLoad = function(){
        formData = [
            {
                type: "settings",
                position: "label-left",
                labelWidth: 100,
                inputWidth: 120
            },
            {
                type: "fieldset",
                label: "角色设置",
                inputWidth: "auto",
                list: [
                    {
                        type: "input",
                        label: "角色名称",
                        name: "name",
                        value: ""
                    },
                    {
                        type: "input",
                        label: "角色描述",
                        name: "desc",
                        value: ""
                    },
                    {
                        type: "input",
                        label: "角色状态",
                        name: "state",
                        value: ""
                    },
                    {
                        type: "input",
                        label: "角色等级",
                        name: "level",
                        value: ""
                    },
                    {
                        type: "button",
                        value: "加载角色1",
                        name: "set1"
                    },
                    {
                        type: "button",
                        value: "加载角色2",
                        name: "set2"
                    },
                    {
                        type: "button",
                        value: "加载角色3",
                        name: "set3"
                    },
                    {
                        type: "button",
                        value: "保存",
                        name: "send"
                    }
                ]
            }
        ];
        myForm = new dhtmlXForm("myForm1", formData);
        var param = new biDwrMethodParam();
        var dwr = new biDwrCaller({
                                      load:function(afterCall, param){
                                          DhtmlxAction.formLoad(param.id, function(data){
                                              afterCall(data);
                                          })
                                      },
                                      loadByAuto:{
                                          methodName:"DhtmlxAction.formLoad",
                                          param:param
                                      },
                                      formSave:{
                                          methodName:"DhtmlxAction.formSave",
                                          success:function(data){
                                              if(data==true){
                                                  alert("保存成功！")
                                              }
                                          }
                                      }
                                  });
        myForm.attachEvent("onButtonClick", function(id){
            switch(id){
                case "set1":{
                    param.setParam(0,12,false);
                    break;
                }
                case "set2":{
                    param.setParam(0,13,false);
                    break;
                }
                case "set3":{
                    param.setParam(0,92,false);
                    break;
                }
                case "send":{
                    myForm.send(dwr.formSave);
                    return;
                }
            }
            myForm.load(dwr.loadByAuto , "json");
        });
    };
    dhx.ready(doOnLoad); //加载成功执行
</script>
<div class="content">
    <div id="myForm1" style="height:230px;"></div>
</div>
</body>
</html>