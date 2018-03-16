var fileamount = 0; //总文件数量
    var xmlhttp; //ajax请求
    var currentpath = new Array();

    //请求文件
    function listFile(filename){
        var filepath = "";
        $.each(currentpath,function(index,value){
            filepath += value + "/";
        });
        filepath += filename;
        xmlhttp=$.ajax({type:"POST",url:"listfile",data:{path:filepath,key:"",sort:""},async:true,success:function(){
            //显示新文件列表
            var xhrt = JSON.parse(xmlhttp.responseText);
            showFile(xhrt);
            $("#filecount").text("选中文件/当前总文件："+0+"/"+fileamount);
            currentpath.push(filename);
            updatepath();
            }
        });    
    }

    //分类文件
    function sortFile(sorttype){
        xmlhttp=$.ajax({type:"POST",url:"listfile",data:{path:"",key:"",sort:sorttype},async:true,success:function(){
            //显示新文件列表
            var xhrt = JSON.parse(xmlhttp.responseText);
            if($.isEmptyObject(xhrt)){
                $("#fileList").empty();
            }else{
                showFile(xhrt);
            }
            $("#filecount").text(sorttype);
            }
        });
    }

    //搜索文件
    function searchFile(){
        var keyword = $("#keyword").val();
        var searchpath = "";
        $.each(currentpath,function(index,value){
            searchpath += value + "/";
        });
        xmlhttp=$.ajax({type:"POST",url:"listfile",data:{path:searchpath,key:keyword,sort:""},async:true,success:function(){
            //显示新文件列表
            var xhrt = JSON.parse(xmlhttp.responseText);
            if($.isEmptyObject(xhrt)){
                showalert("未搜索到符合文件");
            }else{
                showFile(xhrt);
            }
            $("#filecount").text("选中文件/当前总文件："+0+"/"+fileamount);
            }
        });    
    }

    //文件显示
    function showFile(xhrt){
        //清空当前文件
        $("#fileList").empty();
        $("#fileList").off("click");
        sfiles.clear();
        fileamount = 0;
        $.each(xhrt,function(index,content){
            //生成相应文件列表
            var icon = $("<img>");   //图标
            var thumbnail = $("<div></div>").addClass("thumbnail");      //文件缩略图       
            switch (content.type) {
                case ("0"):
                    icon.attr('src',"web/image/dict_icon.png");
                    thumbnail.attr("value",'{"path":"'+content.path+'","type":"0"}');
                    break;
                case ("1"):
                    icon.attr('src',"web/image/file_icon.png");
                    thumbnail.attr("value",'{"path":"'+content.path+'","type":"1"}');
                    break;
                case ("2"):
                    icon.attr('src',"web/image/doc_icon.png");
                    thumbnail.attr("value",'{"path":"'+content.path+'","type":"2"}');
                    break;
                case ("3"):
                    icon.attr('src',"web/image/music_icon.png");
                    thumbnail.attr("value",'{"path":"'+content.path+'","type":"3"}');
                    break;
                case ("4"):
                    icon.attr('src',"web/image/video_icon.png");
                    thumbnail.attr("value",'{"path":"'+content.path+'","type":"4"}');
                    break;
                case ("5"):
                    icon.attr('src',"web/image/zip_icon.png");
                    thumbnail.attr("value",'{"path":"'+content.path+'","type":"5"}');
                    break;
                case ("6"):
                    icon.attr('src',"web/image/apk_icon.png");
                    thumbnail.attr("value",'{"path":"'+content.path+'","type":"6"}');
                    break;
                case ("7"):
                    icon.attr('src',"web/image/img_icon.png");
                    thumbnail.attr("value",'{"path":"'+content.path+'","type":"7"}');
                    break;
                default:
                    icon.attr('src',"web/image/file_icon.png");
                    thumbnail.attr("value",'{"path":"'+content.path+'","type":"1"}');
                    break;
            }
            var name = $("<p></p>").text(content.name).addClass("text-center") .css({   //文件名
                "overflow":"hidden",
                "text-overflow":"ellipsis",
                "white-space":"nowrap" 
            });
            var afile = $("<div></div>").addClass("col-xs-4 col-sm-3 col-md-2 col-lg-1");   //不同大小屏幕每行展示不同文件数量
            thumbnail.append(icon);
            thumbnail.append(name);
            afile.append(thumbnail);
            $("#fileList").append(afile);
            fileamount++;
        });
        $("#fileList").on("click",".thumbnail",function(e){
            e.stopPropagation();
            fileselect(this);
        });
    }

    //下载文件
    function downloadFile(){
        if(sfiles.size>0){
            var cpath = "";
            var files = "";
            $.each(currentpath,function(index,value){
                cpath += value + "/";
            });
            sfiles.forEach(function(item){
                files += item + "/"
            });
            files = files.substring(0,files.length-1);
            //生成下载表单并提交
            var form = $('<form method="POST" action="download">');
            form.append($('<input type="hidden" name="path" value="' + cpath + '">'));
            form.append($('<input type="hidden" name="filenames" value="' + files + '">'));
            form.appendTo('body').submit().remove();
        }else{
            showalert("请选择要下载的文件!");
        }
    }

    //初始化控件
    $(function (){
        $("#sfswitch input").bootstrapSwitch('state',false);  //开关控件
        $('#fileModal').on('hidden.bs.modal', function () {   //Modal控件
            $("#fmodalbody").empty();          //关闭时清除内容
        });
    });

    //inputfile上传设置
    $("#uploadfile").fileinput({
        language : 'zh',
        uploadUrl: 'upload', // 上传的地址
        uploadExtraData: function(){
            var uploadpath = "";
            $.each(currentpath,function(index,value){
                uploadpath += value + "/";
            });
            return {'path':uploadpath};
        },
        //allowedFileExtensions : ['xls','jpg', 'png','gif'],
        maxFileCount: 10,   //同时最多上传10个文件
        //allowedFileTypes: ['image', 'video', 'flash'],  这是允许的文件类型 跟上面的后缀名还不是一回事
        //这是文件名替换
        slugCallback: function(filename) {
            return filename.replace('(', '_').replace(']', '_');
        }
    }).on("fileuploaded", function(event, data) {
        if(data.response)
        {
            refreshFile();
        }
    });

    //刷新文件列表
    function refreshFile(){
        var refreshpath = "";
        $.each(currentpath,function(index,value){
            if(value!=""){
                refreshpath += "/" + value ;
            }
        });
        clickpath(refreshpath);
    }

    //文件选择
    var sfiles = new Set();
    function fileselect(which){
        var jsonval = JSON.parse($(which).attr("value")),
            path = jsonval.path,
            type = jsonval.type,
            name = $(which).find("p").text();
        var flag = $("#sfswitch input").bootstrapSwitch('state');//获取选择模式开关状态
        if(flag){
            if(sfiles.has(name)){
                sfiles.delete(name)
                $(which).css("background-color","white")
            }else{
                sfiles.add(name)
                $(which).css("background-color","#CCCCFF")
            }
            $("#filecount").text("选中文件/当前总文件："+ sfiles.size +"/"+fileamount);
        }else{
            if(type=="0"){
                listFile(name);
            }else{
                openFile(name,path,type);
            }
            
        }
    }

    //打开文件
    function openFile(name,path,type){
        var filepath = "/openfile";
        filepath += path;
        $("#fileModalLabel").text(name);
        $("#fmodalbody").empty();
        switch(type){
            case ("1"):
                break;
            case ("2"):
                break;
            case ("3"):       //打开音频文件
                var audio = $("<audio></audio>").text("浏览器不支持audio标签").addClass("center-block") .attr({"src":filepath,"controls":"controls"});
                $("#fmodalbody").append(audio);
                $("#fileModal").modal('show');
                break;
            case ("4"):      //打开视频文件
                var video = $("<video></video>").text("浏览器不支持video标签").addClass("center-block") .attr({"src":filepath,"controls":"controls"});
                $("#fmodalbody").append(video);
                $("#fileModal").modal('show');
                break;
            case ("5"):
                break;
            case ("6"):
                break;
            case ("7"):      //打开图片文件
                var image = $("<img></img>").addClass("img-responsive center-block") .attr("src",filepath);
                $("#fmodalbody").append(image);
                $("#fileModal").modal('show');
                break;
            default:
                break;
        }
        
    }

    //警告窗
    function showalert(text){
        var alertdiv = $("<div></div>").addClass("alert alert-warning navbar-fixed-bottom");
        var alertclose = $("<a></a>").addClass("close").attr("data-dismiss","alert");
        alertclose.html("&times;");
        alertdiv.append(alertclose);
        alertdiv.append(text);
        $("body").append(alertdiv);
    };

    //重命名弹窗
    function showrename(){
        if(sfiles.size>1){
            //选中文件大于一
            showalert("选择文件过多");
        }else if(sfiles.size == 0){
            //未选中文件
            showalert("请先选择文件");
        }else{
            $("#fileModalLabel").text("重命名");
            $("#fmodalbody").empty();
            var renameform = $("<form></form>").addClass("form-inline");
            var formgroup = $("<div></div>").addClass("form-group");
            var inputname = $("<input>").addClass("form-control").attr({"type":"text","id":"newname"});
            var fnames = sfiles.values();
            sfiles.forEach(function(oldname){
                inputname.val(oldname);
            });
            var renamebtn = $("<button></button>").addClass("btn btn-default").attr("onclick","rename()").html("确定");
            formgroup.append(inputname);
            formgroup.append(renamebtn);
            renameform.append(formgroup);
            $("#fmodalbody").append(renameform);
            $("#fileModal").modal('show');
        }
    }

    //重命名
    function rename(){
        //进行重命名
        var cpath = "";
        $.each(currentpath,function(index,value){
            cpath += value + "/";
        });
        var inputname = $("#newname").val();
        xmlhttp=$.ajax({type:"POST",url:"renamefile",data:{path:cpath,oldname:sfiles[0],newname:inputname},async:true,success:function(){
            refreshFile();
            }
        });    
    }

    //删除文件
    function deleteFile(){
        if(sfiles.size>0){
            var cpath = "";
            var files = "";
            $.each(currentpath,function(index,value){
                cpath += value + "/";
            });
            sfiles.forEach(function(item){
                files += item + "/";
            });
            files = files.substring(0,files.length-1);

            xmlhttp=$.ajax({type:"POST",url:"deletefile",data:{path:cpath,file:files},async:true,success:function(){
                refreshFile();
                }
            });    
        }else{
            showalert("请选择要删除的文件!");
        }
    }

    //路径点击
    function clickpath(path){
        if(path == "/"){
            currentpath = new Array();
            listFile('');
        }else{
            currentpath = path.split("/");
            listFile(currentpath.pop());
        }
    }

    //更新路径
    function updatepath(){
        $("#filepath").empty();
        var cpath = "";
        $.each(currentpath,function(index,value){                     
            var patha = $("<a></a>").text(value);//.attr({href:"javascript:void(0)","onclick":"clickpath(\""+cpath+"\")"});
            if(value==""){
                patha.attr({href:"javascript:void(0)","onclick":"clickpath('')"});
                var home = $("<span></span>").addClass("glyphicon glyphicon-home").attr("aria-hidden",true);
                patha.append(home);
            }else{
                cpath += "/"+value;
                patha.attr({href:"javascript:void(0)","onclick":"clickpath(\""+cpath+"\")"});
            }
            var pathli = $("<li></li>");
            pathli.append(patha);
            $("#filepath").append(pathli);
        });
    }