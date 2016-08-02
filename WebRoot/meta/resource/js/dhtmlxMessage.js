 /******************************************************
 *Copyrights @ 2011，Tianyuan DIC Information Co., Ltd.
 *All rights reserved.
 *
 *Filename：
 *       dhtmlxMessage.js
 *Description：
 *       扩展dhtmlx的Message控件(包括alert框,confirm框,prompt框)
 *Dependent：
 *        dhmtlx.js
 *Author:
 *        王晶
 *Finished：
 *       2011-9-20
 *Modified By：
 *
 * Modified Date:
 *
 * Modified Reasons:
 ********************************************************/

var dhxMessageWins = null;//windows对象
var cssStr = "height:150px;width:310px;margin-top:5px;"; //css样式
var winIndex =1; //win的计数器,用来创建win时赋值给win的id,以区别win
var alIndex =1;//alert框控件的计数器
var conIndex =1;//确认框控件的计数器
var peoIndex =1;//输入框框控件的计数器

/**
  *当dom加载完,初始化windows对象,用来创建window对象
  */
dhx.ready(function(){
  dhxMessageWins = new dhtmlXWindows();
});
function getWin(){
  var win = dhxMessageWins.createWindow("win"+winIndex,100,100,350,200); //定义一个具体的window对象,默认是隐藏状态
  win.button("minmax1").hide();
  win.button("minmax2").hide();
  win.button("park").hide();
  win.center();//设置居中
  win.show();
  win.setModal(true);//加载mask
  winIndex++;
  return win;
}

/**
  *封装alert框,调用方式为dhx.alert();
  *@param content 提示信息 
  */
dhx.alert = function(content){
  var win = getWin();
  win.setText("提示");
  var msgDiv= document.createElement("Div"); //创建window的加载层
  msgDiv.setAttribute("id","alDiv"+alIndex);
  document.body.appendChild(msgDiv);
  msgDiv.style.cssText = cssStr;
  var attachDiv = document.getElementById("alDiv"+alIndex);
  var textDiv = document.createElement("Div"); //创建window的文字层
  textDiv.setAttribute("id","alTxtDiv"+alIndex);
  textDiv.style.cssText="height:100px;width:310px;padding-left:10px;";//层的样式
  textDiv.innerHTML= content;
  attachDiv.appendChild(textDiv); //将文字的层添加到win的层中
  var btnDiv = document.createElement("Div"); //创建window的按钮层
  btnDiv.setAttribute("id","alBtnDiv"+alIndex);
  btnDiv.style.cssText="height:20px;width:310px; margin-left:80px;";//在加载form层加载样式,这样可以设置按钮的位置
  attachDiv.appendChild(btnDiv);//加载按钮层到win层中
  win.attachObject(attachDiv);//win加载到层上
  var data =[{     //创建form表单
           type: "label",
           list:[{type: "button",
                  value: "确定",
                  name:'ok'
           }]
       }]
   var myForm = new dhtmlXForm("alBtnDiv"+alIndex, data);
   myForm.attachEvent("onButtonClick", function(name,command){
          win.hide();
          win.setModal(false);
          msgDiv = null;
          textDiv =null;
          btnDiv = null;
          delete textDiv;
          delete btnDiv;
          delete msgDiv;
   });
   win.attachEvent("onClose",function(w){
       w.hide();
       w.setModal(false);
       textDiv =null;
       btnDiv = null;
       msgDiv = null;
       delete textDiv;
       delete btnDiv;
       delete msgDiv;
   });
   alIndex++;
}

/**
  *封装confirm框,调用方式为dhx.confirm();
  *@param content confirm的提示信息 例如:你确定删除?
  *@param callback 回调函数
  */
dhx.confirm = function(content,callback){
  var win = getWin();
  win.setText("确认");
  var msgDiv= document.createElement("Div");//创建win装载层
  msgDiv.setAttribute("id","conDiv"+conIndex);
  document.body.appendChild(msgDiv);
  msgDiv.style.cssText = cssStr;
  var attachDiv = document.getElementById("conDiv"+conIndex);
  var textDiv = document.createElement("Div"); //创建window的文字层
  textDiv.setAttribute("id","conTxtDiv"+conIndex);
  textDiv.style.cssText="height:100px;width:310px;padding-left:5px;";
  textDiv.innerHTML= content;
  attachDiv.appendChild(textDiv);
  var btnDiv = document.createElement("Div"); //创建window的按钮层
  btnDiv.setAttribute("id","conBtnDiv"+conIndex);
  btnDiv.style.cssText="height:0px;width:310px;margin-left:60px;";
  attachDiv.appendChild(btnDiv);
  win.attachObject(attachDiv);
  var data =[{
    type: "label",
    list:[{type: "button",
           value: "确定",
           name:'ok',
           command:callback
           },{
           type: "newcolumn"
           },{
           type: "button",
           value: "退出",
           name:'cancel',
           command:function(){
               win.hide();
               win.setModal(false);
               textDiv =null;
               btnDiv = null;
               msgDiv = null;
               delete textDiv;
               delete btnDiv;
               delete msgDiv; 
           }
     }]
    }]
  var myForm = new dhtmlXForm("conBtnDiv"+conIndex, data);
  myForm.attachEvent("onButtonClick", function(name,command){
      win.hide();
      win.setModal(false);
      command();
      myForm = null;
      textDiv =null;
      btnDiv = null;
      msgDiv = null;
      delete textDiv;
      delete btnDiv;
      delete msgDiv; 
  });
  win.attachEvent("onClose",function(w){
    w.hide();
    w.setModal(false);
    msgDiv = null;
    textDiv =null;
    btnDiv = null;
    delete textDiv;
    delete btnDiv;
    delete msgDiv; 
  });
  conIndex++;
}

/**
  *封装prompt框,调用方式为dhx.prompt();
  *@param content prompt的提示信息 例如:请输入姓名?
  *@param callback 回调函数
  */
dhx.prompt = function(content,callback){
  var win = getWin();
  win.setText("输入");
  var msgDiv= document.createElement("Div");
  msgDiv.setAttribute("id","proDiv"+peoIndex);
  document.body.appendChild(msgDiv);
  msgDiv.style.cssText = cssStr;
  var attachDiv = document.getElementById("proDiv"+peoIndex);
  var textDiv = document.createElement("Div"); //创建window的文字层
  textDiv.setAttribute("id","proTxtDiv"+peoIndex);
  textDiv.style.cssText="height:100px;width:310px;padding-left:5px;";
  var html='<input type="text" width="120"/>';
  textDiv.innerHTML= content+"<p></p>"+html;
  attachDiv.appendChild(textDiv);
  var btnDiv = document.createElement("Div"); //创建window的按钮层
  btnDiv.setAttribute("id","proBtnDiv"+peoIndex);
  btnDiv.style.cssText="height:0px;width:310px;margin-left:60px;";
  attachDiv.appendChild(btnDiv);
  win.attachObject(attachDiv);
  var data =[{
        type: "label",
        list:[{type: "button",
               value: "确定",
               name:'ok',
               command:callback
               },{
               type: "newcolumn"
               },{
               type: "button",
               value: "退出",
               name:'cancel',
               command:function(){
                   win.hide();
                   win.setModal(false);
                   textDiv =null;
                   btnDiv = null;
                   delete textDiv;
                   delete btnDiv;
                   msgDiv = null;
                   delete msgDiv; 
               }
         }]
   }]
  var myForm = new dhtmlXForm("proBtnDiv"+peoIndex, data);
  myForm.attachEvent("onButtonClick", function(name, command){
     if(name=="ok"){
         var ipts = attachDiv.getElementsByTagName("input");
         var value= null;  
         if(ipts.length <1 || ipts[0].value==''){
             return ;
         }else{
             value =ipts[0].value;
             win.hide();
             win.setModal(false);
             command(value);
             textDiv =null;
             btnDiv = null;
             delete textDiv;
             delete btnDiv;
             msgDiv = null;
             delete msgDiv; 
       }//end elseokBtn
     }//end if
  });//end formFun
  win.attachEvent("onClose",function(w){
     win.hide();
     win.setModal(false);
     textDiv =null;
     btnDiv = null;
     delete textDiv;
     delete btnDiv;
     msgDiv = null;
     delete msgDiv; 
  });
       peoIndex++;
}