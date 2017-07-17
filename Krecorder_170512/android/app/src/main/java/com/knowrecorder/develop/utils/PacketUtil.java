package com.knowrecorder.develop.utils;

import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;

import com.google.gson.Gson;
import com.knowrecorder.Toolbox.Toolbox;
import com.knowrecorder.develop.ProcessStateModel;
import com.knowrecorder.develop.controller.RealmPacketPutter;
import com.knowrecorder.develop.manager.PageManager;
import com.knowrecorder.develop.model.body.RedoBody;
import com.knowrecorder.develop.model.body.UndoBody;
import com.knowrecorder.develop.model.packetHolder.DrawingPacket;
import com.knowrecorder.develop.model.packetHolder.ObjectControllPacket;
import com.knowrecorder.develop.model.packetHolder.ObjectCreatePacket;
import com.knowrecorder.develop.model.body.ChangePageBody;
import com.knowrecorder.develop.model.body.DelObjBody;
import com.knowrecorder.develop.model.body.GrabObjBody;
import com.knowrecorder.develop.model.body.ImageBody;
import com.knowrecorder.develop.model.body.LaserBody;
import com.knowrecorder.develop.model.body.PdfBody;
import com.knowrecorder.develop.model.body.PenBody;
import com.knowrecorder.develop.model.body.PointEndBody;
import com.knowrecorder.develop.model.body.PointMoveBody;
import com.knowrecorder.develop.model.body.ResizeObjBody;
import com.knowrecorder.develop.model.body.ShapeBody;
import com.knowrecorder.develop.model.body.TransObjBody;
import com.knowrecorder.develop.model.body.TxtBeginBody;
import com.knowrecorder.develop.model.body.TxtEditBody;
import com.knowrecorder.develop.model.body.TxtEndBody;
import com.knowrecorder.develop.model.body.VideoBody;
import com.knowrecorder.develop.model.body.VideoPauseBody;
import com.knowrecorder.develop.model.body.VideoProgressingBody;
import com.knowrecorder.develop.model.body.VideoStartBody;
import com.knowrecorder.develop.model.body.VolumeProgressingBody;
import com.knowrecorder.develop.model.realmHoler.PacketObjectHolder;
import com.knowrecorder.develop.papers.DrawingPanel;

import static java.lang.Math.abs;

/**
 * Created by changha on 2017-02-13.
 */

public class PacketUtil {
	public static final int PEN 							= 1;
	public static final int POINTMOVE 					= 2;
	public static final int POINTEND 					= 3;
	public static final int LASER 						= 4;
	public static final int SHAPE 						= 5;
	public static final int GRABOBJ 					= 6;
	public static final int TRANSOBJ 					= 7;
	public static final int RESIZEOBJ 					= 8;
	public static final int ROTATEOBJ 					= 9;
	public static final int VIDEO 						= 10;
	public static final int VIDEOSTART 				= 11;
	public static final int VIDEOPAUSE 				= 12;
	public static final int VIDEOPROGRESSING 			= 13;
	public static final int VOLUMEPROGRESSING 		= 14;
	public static final int TXTBEGIN 					= 15;
	public static final int TXTEDIT						= 16;
	public static final int TXTEND						= 17;
	public static final int ZOOMBEGIN 					= 18;
	public static final int ZOOM						= 19;
	public static final int ZOOMEND						= 20;
	public static final int DELOBJ 						= 21;
	public static final int ADDPAGE 					= 22;
	public static final int CHANGEPAGE 				= 23;
	public static final int IMAGE 						= 24;
	public static final int PDF 							= 25;
	public static final int PDFCHANGEPAGE 				= 26;
	public static final int UNDO 						= 27;
	public static final int REDO 						= 28;
	public static final int ALLDELETE 					= 29;
	public static final int TXT						 	= 30;
	public static final int TXTMOVE 					= 31;
	public static final int TXTRESIZE 					= 32;

	public static final String S_PEN 					= "pen";
	public static final String S_POINTMOVE 				= "pointmove";
	public static final String S_POINTEND 				= "pointend";
	public static final String S_LASER 					= "laser";
	public static final String S_SHAPE 					= "shape";
	public static final String S_GRABOBJ 				= "grabobj";
	public static final String S_TRANSOBJ 				= "transobj";
	public static final String S_RESIZEOBJ				= "resizeobj";
	public static final String S_ROTATEOBJ 				= "rotateobj";
	public static final String S_IMAGE 					= "image";                         //10
	public static final String S_VIDEO 					= "video";
	public static final String S_VIDEOSTART 			= "videostart";
	public static final String S_VIDEOPAUSE 			= "videopause";
	public static final String S_VIDEOPROGRESSING 		= "videoprogressing";
	public static final String S_VOLUMEPROGRESSING 	= "volumeprogressing";
	public static final String S_TXTBEGIN 				= "txtbegin";
	public static final String S_TXTEDIT 				= "txtedit";
	public static final String S_TXTEND 				= "txtend";
	public static final String S_ZOOMBEGIN 				= "zoombegin";
	public static final String S_ZOOM 					= "zoom";                          //20
	public static final String S_ZOOMEND 				= "zoomend";
	public static final String S_DELOBJ 				= "delobj";
	public static final String S_ADDPAGE 				= "addpage";
	public static final String S_CHANGEPAGE 			= "changepage";
	public static final String S_PDF 					= "pdf";
	public static final String S_PDFCHANGEPAGE 		= "pdfchangepage";
	public static final String S_UNDO 					= "undo";
	public static final String S_REDO 					= "redo";
	public static final String S_ALLDELETE 				= "alldelete";//todo 기능 추가 해야함
	public static final String S_TXT 					= "txt";                          //30
	public static final String S_TXTMOVE 				= "txtmove";
	public static final String S_TXTRESIZE 				= "txtresize";


	public static long currentMid = -1;
	public static float startRun = -1;
	public static float endRun = -1;
	public static String packetType;

	public static void insertTimeLine(long mid, float runtime, String type){
		if(currentMid  == -1){
			currentMid = mid;
			startRun = runtime;
			packetType = type;

		}else if(currentMid != mid){

			if(endRun == -1)
				endRun = startRun;

			RealmPacketPutter.getInstance().InsertTimeLine(currentMid, startRun, endRun, packetType);

			currentMid = mid;
			startRun = runtime;
			packetType = type;
			endRun = -1;

		}else{
			endRun = runtime;
		}
	}
	public static void insertTimeLine(){
		if(currentMid != -1) {

			if(endRun  == -1)
				endRun = startRun;

			RealmPacketPutter.getInstance().InsertTimeLine(currentMid, startRun, endRun, packetType);
			currentMid = -1;
			startRun = -1;
			endRun = -1;
			packetType = "";
		}
	}

	public static void makePacket(long mid, DrawingPacket drawingHolder){

		int id = DrawingPanel.id.incrementAndGet();
		long pageId = PageManager.getInstance().getCurrentPageId();
		long runTime = ProcessStateModel.getInstanse().getElapsedTime();
		String type = drawingHolder.getType();
		String body="";

		if(ProcessStateModel.getInstanse().isRecording())
			insertTimeLine(mid, runTime, type);

		if(TextUtils.equals(type, S_PEN))
			body = penBody(drawingHolder);
		else if(TextUtils.equals(type, S_POINTMOVE))
			body = pointMoveBody(drawingHolder.getX(), drawingHolder.getY());
		else if(TextUtils.equals(type, S_POINTEND))
			body = pointEndBody(drawingHolder.getX(), drawingHolder.getY());
		else if(TextUtils.equals(type, S_LASER))
			body = laserBody(drawingHolder);

		PacketObjectHolder packet = new PacketObjectHolder.PacketObjectHolderBuilder(id, mid, pageId, type, body, runTime)
				.setIsStaticEnabled(!ProcessStateModel.getInstanse().isRecording()).build();
		Log.d("drawingPacket", "id : "+id+" mid : "+mid+" pageId : "+pageId+" type : "+type+" runtime : "+runTime+"\nbody : "+body);
		RealmPacketPutter.getInstance().packetPut(packet.clone());
	}

	public static void makePacket(long mid, ObjectCreatePacket createHolder){
		makePacket(mid, createHolder, PageManager.getInstance().getCurrentPageId());
	}
	public static void makePacket(long mid, ObjectCreatePacket createHolder, long pageId){
		int id = DrawingPanel.id.incrementAndGet();
		long runTime = ProcessStateModel.getInstanse().getElapsedTime();
		String type = createHolder.getType();
		String body="";
		if(ProcessStateModel.getInstanse().isRecording())
			insertTimeLine(mid, runTime, type);

		if(TextUtils.equals(type, S_SHAPE))
			body = shapeBody(createHolder);
		else if(TextUtils.equals(type, S_POINTMOVE))
			body = pointMoveBody(createHolder.getOriginX(), createHolder.getOriginY());
		else if(TextUtils.equals(type, S_POINTEND))
			body = pointEndBody(createHolder.getOriginX(), createHolder.getOriginY());
		else if(TextUtils.equals(type, S_IMAGE))
			body = imageBody(createHolder);
		else if(TextUtils.equals(type, S_VIDEO))
			body = videoBody(createHolder);
		else if(TextUtils.equals(type, S_TXTBEGIN))
			body = txtBeginBody(createHolder);
		else if(TextUtils.equals(type, S_PDF))
			body = pdfBody(createHolder);

		PacketObjectHolder packet = new PacketObjectHolder.PacketObjectHolderBuilder(id, mid, pageId, type, body, runTime)
				.setIsStaticEnabled(!ProcessStateModel.getInstanse().isRecording()).build();
		Log.d("drawingPacket", "id : "+id+" mid : "+mid+" pageId : "+pageId+" type : "+type+" runtime : "+runTime+"\nbody : "+body);
		RealmPacketPutter.getInstance().packetPut(packet.clone());
	}
	public static void makePacket(long mid, ObjectControllPacket controllHolder){
		int id = DrawingPanel.id.incrementAndGet();
		long pageId = PageManager.getInstance().getCurrentPageId();
		long runTime = ProcessStateModel.getInstanse().getElapsedTime();
		String type = controllHolder.getType();
		String body="";
		if(ProcessStateModel.getInstanse().isRecording())
			insertTimeLine(mid, runTime, type);

		if(TextUtils.equals(type, S_GRABOBJ))
			body = grabobjBody(controllHolder);
		else if(TextUtils.equals(type, S_TRANSOBJ))
			body = transobjBody(controllHolder);
		else if(TextUtils.equals(type, S_RESIZEOBJ))
			body = resizeobjBody(controllHolder);
		else if(TextUtils.equals(type, S_VIDEOSTART))
			body = videoStartBody(controllHolder);
		else if(TextUtils.equals(type, S_VIDEOPAUSE))
			body = videoPauseBody(controllHolder);
		else if(TextUtils.equals(type, S_VIDEOPROGRESSING))
			body = videoProgressing(controllHolder);
		else if(TextUtils.equals(type, S_VOLUMEPROGRESSING))
			body = volumePregressing(controllHolder);
		else if(TextUtils.equals(type, S_TXTEDIT))
			body = txtEditBody(controllHolder);
		else if(TextUtils.equals(type, S_TXTEND))
			body = txtEndBody(controllHolder);
		else if(TextUtils.equals(type, S_DELOBJ))
			body = delObjBody(controllHolder);
		else if(TextUtils.equals(type, S_CHANGEPAGE)) {
			body = changePageBody(controllHolder);
		}else if(TextUtils.equals(type, S_UNDO))
			body = undoBody(controllHolder);
		else if(TextUtils.equals(type, S_REDO))
			body = redoBody(controllHolder);

		PacketObjectHolder packet = new PacketObjectHolder.PacketObjectHolderBuilder(id, mid, pageId, type, body, runTime)
				.setIsStaticEnabled(!ProcessStateModel.getInstanse().isRecording()).build();
		Log.d("drawingPacket", "id : "+id+" mid : "+mid+" pageId : "+pageId+" type : "+type+" runtime : "+runTime+"\nbody : "+body);
		RealmPacketPutter.getInstance().packetPut(packet.clone());
	}

	private static String penBody(DrawingPacket drawingPacket){
		Gson gson = new Gson();
		boolean iseraser = Toolbox.getInstance().getToolType() == Toolbox.Tooltype.PEN ? false : true;
		float stkwidth = Toolbox.getInstance().getToolType() == Toolbox.Tooltype.PEN ? Toolbox.getInstance().currentStrokeWidth : Toolbox.getInstance().currentEraserWidth;
		float angle=0f;
		long color = ColorUtil.ARGBFromColor(Toolbox.getInstance().currentStrokeColor);
		int b = 1, e = 0;
		float opacity=(float)Toolbox.getInstance().currentStrokeOpacity/255;

		String pointsString = gson.toJson(drawingPacket.getPoints());
		float[][] pointsArray = gson.fromJson(pointsString, float[][].class);

		PenBody penBody = new PenBody(iseraser, stkwidth, angle, color, b, e, opacity, pointsArray);

		return gson.toJson(penBody);
	}
	private static String pointMoveBody(float x, float y){
		Gson gson = new Gson();
		int b = 0 , e = 0;
		float stkwidth = Toolbox.getInstance().currentStrokeWidth;

		PointMoveBody pointMove = new PointMoveBody(b, e, x, y, stkwidth);

		return gson.toJson(pointMove);
	}
	private static String pointEndBody(float x, float y){
		Gson gson = new Gson();
		boolean iseraser = Toolbox.getInstance().getToolType() == Toolbox.Tooltype.PEN ? false : true;
		int b = 0 , e = 1;
		float stkwidth = Toolbox.getInstance().currentStrokeWidth;

		PointEndBody pointEnd = new PointEndBody(iseraser, b, e, x, y, stkwidth);

		return gson.toJson(pointEnd);
	}
	private static String laserBody(DrawingPacket drawingPacket){
		Gson gson = new Gson();
		int ico  = Toolbox.getInstance().getCurrentPointer();
		int pointertag  = Toolbox.getInstance().getCurrentPointerTag() ; //todo shoap color type 넣기
		int[] be = getBeginEndValue(drawingPacket.getAction());


		LaserBody laserBody = new LaserBody(drawingPacket.getX(), drawingPacket.getY(), ico, pointertag, be[0], be[1]);

		return gson.toJson(laserBody);
	}

	private static String shapeBody(ObjectCreatePacket packetHolder){
		Gson gson = new Gson();
		int shapetype = Toolbox.getInstance().currentShape.ordinal();
		float beginx = packetHolder.getOriginX();
		float beginy = packetHolder.getOriginY();
		float endx = packetHolder.getEndX();
		float endy = packetHolder.getEndY();
		float angle = 0f;
		float scale = packetHolder.getScale();
		long color = ColorUtil.ARGBFromColor(Toolbox.getInstance().currentShapeColor);
		int[] be = getBeginEndValue(packetHolder.getAction());

		ShapeBody shapeBody = new ShapeBody(convertShapeType(shapetype), beginx, beginy, endx, endy, angle, scale, color, be[0], be[1]);

		return gson.toJson(shapeBody);
	}
	private static String imageBody(ObjectCreatePacket packetHolder){
		Gson gson = new Gson();
		ImageBody imageBody = new ImageBody(packetHolder.getScale(), packetHolder.getOriginX(), packetHolder.getOriginY(), packetHolder.getEndX(), packetHolder.getEndY(), packetHolder.getW(), packetHolder.getH());

		return gson.toJson(imageBody);
	}
	private static String videoBody(ObjectCreatePacket packetHolder){
		Gson gson = new Gson();
		VideoBody videoBody = new VideoBody(packetHolder.getScale(), packetHolder.getOriginX(), packetHolder.getOriginY(), packetHolder.getEndX(), packetHolder.getEndY(), packetHolder.getW(), packetHolder.getH(), packetHolder.getVideoprogress(), packetHolder.getVolume());
		return gson.toJson(videoBody);
	}
	private static String txtBeginBody(ObjectCreatePacket packetHolder) {
		Gson gson = new Gson();
		int[] be = getBeginEndValue(packetHolder.getAction());
		TxtBeginBody txtBeginBody = new TxtBeginBody(be[0], be[1], packetHolder.getOriginX(), packetHolder.getOriginY(), packetHolder.getEndX(), packetHolder.getEndY(), packetHolder.getW(), packetHolder.getH(), "");
		return gson.toJson(txtBeginBody);
	}
	private static String pdfBody(ObjectCreatePacket packetHolder){
		Gson gson = new Gson();
		int[] be = getBeginEndValue(packetHolder.getAction());
		PdfBody pdfBody = new PdfBody(be[0], be[1], packetHolder.getOriginX(), packetHolder.getOriginY(), packetHolder.getPdfpageno(), packetHolder.getFilename());
		return gson
				.toJson(pdfBody);
	}

	private static String grabobjBody(ObjectControllPacket controllPacket){
		Gson gson = new Gson();
		int[] be = getBeginEndValue(controllPacket.getAction());
		GrabObjBody grabObjBody = new GrabObjBody(be[0], be[1], controllPacket.getTarget(), controllPacket.isGrabbed());

		return gson.toJson(grabObjBody);
	}
	private static String transobjBody(ObjectControllPacket controllPacket){
		Gson gson = new Gson();
		int[] be = getBeginEndValue(controllPacket.getAction());
		TransObjBody transObjBody = new TransObjBody(be[0], be[1],controllPacket.getTarget(), controllPacket.getDx(), controllPacket.getDy());

		return gson.toJson(transObjBody);
	}
	private static String resizeobjBody(ObjectControllPacket controllPacket){
		Gson gson = new Gson();
		int[] be = getBeginEndValue(controllPacket.getAction());

		ResizeObjBody resizeObjBody = new ResizeObjBody(be[0], be[1], controllPacket.getTarget(), controllPacket.getScale());

		return gson.toJson(resizeObjBody);
	}
	private static String videoStartBody(ObjectControllPacket controllPacket){
		Gson gson = new Gson();
		int[] be = getBeginEndValue(controllPacket.getAction());
		VideoStartBody videoStartBody = new VideoStartBody(be[0], be[1], controllPacket.getTarget(), controllPacket.getStartvalue(), controllPacket.getTotalVideoTime());
		return gson.toJson(videoStartBody);
	}
	private static String videoPauseBody(ObjectControllPacket controllPacket){
		Gson gson = new Gson();
		int[] be = getBeginEndValue(controllPacket.getAction());
		VideoPauseBody videoPauseBody = new VideoPauseBody(be[0], be[1], controllPacket.getTarget(), controllPacket.getEndvalue(), controllPacket.getTotalVideoTime());
		return gson.toJson(videoPauseBody);
	}
	private static String videoProgressing(ObjectControllPacket controllPacket){
		Gson gson = new Gson();
		int[] be = getBeginEndValue(controllPacket.getAction());
		VideoProgressingBody videoProgressingBody = new VideoProgressingBody(be[0], be[1], controllPacket.getTarget(), controllPacket.getVideoprogress());
		return gson.toJson(videoProgressingBody);
	}
	private static String volumePregressing(ObjectControllPacket controllPacket){
		Gson gson = new Gson();
		int[] be = getBeginEndValue(controllPacket.getAction());
		VolumeProgressingBody videoProgressingBody = new VolumeProgressingBody(be[0], be[1], controllPacket.getVolumeprogress(), controllPacket.getTarget());
		return gson.toJson(videoProgressingBody);
	}
	private static String txtEditBody(ObjectControllPacket controllPacket){
		Gson gson = new Gson();
		int[] be = getBeginEndValue(controllPacket.getAction());
		TxtEditBody txtEditBody = new TxtEditBody(be[0], be[1], controllPacket.getTarget(), controllPacket.getContent());
		return gson.toJson(txtEditBody);
	}
	private static String txtEndBody(ObjectControllPacket controllPacket){
		Gson gson = new Gson();
		int[] be = getBeginEndValue(controllPacket.getAction());
		TxtEndBody txtEndBody = new TxtEndBody(be[0], be[1], controllPacket.getTarget(), controllPacket.getContent());
		return gson.toJson(txtEndBody);
	}
	private static String delObjBody(ObjectControllPacket controllPacket){
		Gson gson = new Gson();
		int[] be = getBeginEndValue(controllPacket.getAction());
		DelObjBody delObjBody = new DelObjBody(be[0], be[1], controllPacket.getTarget());
		return gson.toJson(delObjBody);
	}
	private static String changePageBody(ObjectControllPacket controllPacket){
		Gson gson = new Gson();
		int[] be = getBeginEndValue(controllPacket.getAction());
		ChangePageBody changePageBody = new ChangePageBody(be[0], be[1], controllPacket.getPageno());
		return gson.toJson(changePageBody);
	}
	private static String undoBody(ObjectControllPacket controllPacket){
		Gson gson = new Gson();
		int[] be = getBeginEndValue(controllPacket.getAction());
		UndoBody undoBody = new UndoBody(be[0], be[1], controllPacket.getTarget());
		return gson.toJson(undoBody);
	}
	private static String redoBody(ObjectControllPacket controllPacket){
		Gson gson = new Gson();
		int[] be = getBeginEndValue(controllPacket.getAction());
		RedoBody redoBo = new RedoBody(be[0], be[1], controllPacket.getTarget());
		return gson.toJson(redoBo);
	}


	private static int[] getBeginEndValue(int action){
		int[] be = new int[2];
		switch (action){
			case MotionEvent.ACTION_DOWN :
				be[0] = 1;
				be[1] = 0;
				break;
			case MotionEvent.ACTION_MOVE :
				be[0] = 0;
				be[1] = 0;
				break;
			case MotionEvent.ACTION_UP :
				be[0] = 0;
				be[1] = 1;
				break;
			default:
				be[0] = 1;
				be[1] = 1;
		}
		return be;
	}
	private static String convertShapeType(int type){ //todo IOS 떄문에 쓸데없이 만든 메소드
		switch (type){
			case 0 :
				return "circle";
			case 1 :
				return "tri";
			case 2 :
				return "box";
		}
		return "";
	}
}