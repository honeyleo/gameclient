////////////////////////////////////////////////////////////////////
//                            _ooOoo_                             //
//                           o8888888o                            //    
//                           88" . "88                            //    
//                           (| ^_^ |)                            //    
//                           O\  =  /O                            //
//                        ____/`---'\____                         //                        
//                      .'  \\|     |//  `.                       //
//                     /  \\|||  :  |||//  \                      //    
//                    /  _||||| -:- |||||-  \                     //
//                    |   | \\\  -  /// |   |                     //
//                    | \_|  ''\---/''  |   |                     //        
//                    \  .-\__  `-`  ___/-. /                     //        
//                  ___`. .'  /--.--\  `. . ___                   //    
//                ."" '<  `.___\_<|>_/___.'  >'"".                //
//              | | :  `- \`.;`\ _ /`;.`/ - ` : | |               //    
//              \  \ `-.   \_ __\ /__ _/   .-` /  /               //
//        ========`-.____`-.___\_____/___.-`____.-'========       //    
//                             `=---='                            //
//        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^      //         
//                       佛祖镇楼                  BUG辟易						  //
//          	佛曰:          									  //
//                  	写字楼里写字间，写字间里程序员；                  				  //
//                  	程序人员写程序，又拿程序换酒钱。						  //
//                  	酒醒只在网上坐，酒醉还来网下眠；						  //
//                  	酒醉酒醒日复日，网上网下年复年。                                                                            //
//                  	但愿老死电脑间，不愿鞠躬老板前；                                                                            //
//                  	奔驰宝马贵者趣，公交自行程序员。                                                                            //
//                  	别人笑我忒疯癫，我笑自己命太贱；                                                                            //
//                  	不见满街漂亮妹，哪个归得程序员？                                                                            //
//                                                                //
////////////////////////////////////////////////////////////////////
package cn.huizhi.client;

import java.util.Map;

import cn.huizhi.net.AppMessage;
import cn.huizhi.util.Config;
import cn.lfyun.network.message.LoginByPidReq_Protocol.LoginByPidReq;

import com.google.common.collect.Maps;

/**
 * @copyright SHENZHEN RONG WANG HUI ZHI TECHNOLOGY CORP
 * @author Lyon.liao
 * 创建时间：2014年10月8日
 * 类说明：
 * 
 * 最后修改时间：2014年10月8日
 * 修改内容： 新建此类
 *************************************************************
 *                                    .. .vr       
 *                                qBMBBBMBMY     
 *                              8BBBBBOBMBMv    
 *                            iMBMM5vOY:BMBBv        
 *            .r,             OBM;   .: rBBBBBY     
 *            vUL             7BB   .;7. LBMMBBM.   
 *           .@Wwz.           :uvir .i:.iLMOMOBM..  
 *            vv::r;             iY. ...rv,@arqiao. 
 *             Li. i:             v:.::::7vOBBMBL.. 
 *             ,i7: vSUi,         :M7.:.,:u08OP. .  
 *               .N2k5u1ju7,..     BMGiiL7   ,i,i.  
 *                :rLjFYjvjLY7r::.  ;v  vr... rE8q;.:,, 
 *               751jSLXPFu5uU@guohezou.,1vjY2E8@Yizero.    
 *               BB:FMu rkM8Eq0PFjF15FZ0Xu15F25uuLuu25Gi.   
 *             ivSvvXL    :v58ZOGZXF2UUkFSFkU1u125uUJUUZ,   
 *           :@kevensun.      ,iY20GOXSUXkSuS2F5XXkUX5SEv.  
 *       .:i0BMBMBBOOBMUi;,        ,;8PkFP5NkPXkFqPEqqkZu.  
 *     .rqMqBBMOMMBMBBBM .           @Mars.KDIDS11kFSU5q5   
 *   .7BBOi1L1MM8BBBOMBB..,          8kqS52XkkU1Uqkk1kUEJ   
 *   .;MBZ;iiMBMBMMOBBBu ,           1OkS1F1X5kPP112F51kU   
 *     .rPY  OMBMBBBMBB2 ,.          rME5SSSFk1XPqFNkSUPZ,.
 *            ;;JuBML::r:.:.,,        SZPX0SXSP5kXGNP15UBr.
 *                L,    :@huhao.      :MNZqNXqSqXk2E0PSXPE .
 *            viLBX.,,v8Bj. i:r7:,     2Zkqq0XXSNN0NOXXSXOU 
 *          :r2. rMBGBMGi .7Y, 1i::i   vO0PMNNSXXEqP@Secbone.
 *          .i1r. .jkY,    vE. iY....  20Fq0q5X5F1S2F22uuv1M;
 *
 ***************************************************************
 */
public class ClientMain {

	private static String ip = Config.getValue("ip");
	private static int port = Config.getIntValue("port");
	
	private static Map<Integer, ProtobufClient> clientMap = Maps.newConcurrentMap();
	
	public static void main(String[] args) {
		ClientMain clientMain = new ClientMain();
		clientMain.login();
	}
	
	public void login() {
		int startPid = Config.getIntValue("startPid");
		int endPid = Config.getIntValue("endPid");
		for(int i = startPid; i < endPid; i ++) {
			LoginByPidReq.Builder loginBuilder = LoginByPidReq.newBuilder();
			loginBuilder.setPid(i);
			ProtobufClient client = ProtobufClient.asynStart(ip, port, i, AppMessage.CMD_LOGIN_BY_PID_REQ, loginBuilder);
			clientMap.put(i, client);
		}
	}
}
