//package com.knowrecorder.Utils;
//
///**
// * Created by ssyou on 2016-10-05.
// */
//
//public class EventParser {
//    public static String getEventFromPacket(Packet packet) {
//        String event = "";
//
//        switch (packet.getCommand()) {
//            case "Drawing":
//                event = packet.getDrawingModel().getEvent();
//                break;
//
//            case "Object":
//                event = packet.getObjectModel().getEvent();
//                break;
//
//            case "Text":
//                event = packet.getTextModel().getEvent();
//                break;
//
//            case "Video":
//                event = packet.getVideoModel().getEvent();
//                break;
//        }
//
//        return event;
//    }
//}
