 /******************************************************
 *Copyrights @ 2011，Tianyuan DIC Information Co., Ltd.
 *All rights reserved.
 *
 *Filename：
 *       basicException.js
 *Description：
 *       整个项目的异常框架的js文件
 *       用于页面出错或后台异常的时候,给开发人员的调试信息(前台页面出错信息和后台java代码异常)
 *       1.定义了页面出错的处理方法(弹出提示框给开发人员,包括出错信息,出错的页面和页面中的具体行数)
 *       2.封装了dhtmlx的弹出框   
 *Dependent：
 *        dhmtlx.js
 *Author:
 *        王晶
 *Finished：
 *       2011-9-13
 *Modified By：
 *
 * Modified Date:
 *
 * Modified Reasons:

 ********************************************************/
var dhxExceptionWins = null; //申明全局的window的set
dhx.ready(function(){
	dhxExceptionWins = new dhtmlXWindows();
});
/**
  * 获取windows的对象,创建默认具体的window对象来模拟弹出框
  * @param id 窗口的id
  * @param divId 层的id,等框架页面建好,可不用传这个参数
  * @param msg 出错的信息
  */
function showDefaultExceptionInfo(id,divId,msg){
    var win = dhxExceptionWins.createWindow("win"+id,100,100,300,100); //定义一个具体的window对象,默认是隐藏状态
    win.button("minmax1").hide(); //设置各种窗口中的按钮隐藏,模拟信息提示框
    win.button("minmax2").hide();
    win.button("park").hide();
    win.attachObject(divId);
    document.getElementById(divId).innerHTML=msg;
    win.show();
 }
      
/**
 * 获取windows的对象,创建默认自定义的window对象来模拟弹出框,这个方法暂时未使用
 * @param id 窗口的id
 * @param divId 层的id,等框架页面建好,可不用传这个参数
 * @param width 窗口的宽
 * @param height 窗口的高
 * @param msg 出错的信息
 */
function showExceptionInfo(id,divId,width,height,msg){
    var win = dhxExceptionWins.createWindow("win"+id,100,100,width,height); 
    win.button("minmax1").hide();
    win.button("minmax2").hide();
    win.button("park").hide();
    win.attachObject(divId);
    document.getElementById(divId).innerHTML=msg;
}
      
/**
 * 页面出错的方法
 */
window.onerror = function(msg,url,line){
    var info= "当前页面错误信息是:"+msg+"  "+"出错页面是:"+url+"  "+"出错行数是:"+line;
    alert(info);
    return true;
}
   
/**
 * 超时的处理方法,需要在function中定义超时具体做什么
 * @param fun 设置超时的处理函数
 * @param t    超时的时间
 */
 //window.setTimeout(fun,t);