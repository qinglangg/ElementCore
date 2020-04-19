package com.elementtimes.elementcore.api.misc.wrapper;

import com.elementtimes.elementcore.api.ECModElements;

import java.util.List;

public class NetEventWrapper {

    public Object eventHandler;

    public NetEventWrapper(Object handler) {
        eventHandler = handler;
    }

    public void apply(ECModElements elements) {
        if (elements.eventChannel != null && eventHandler != null) {
            elements.warn("    {}", eventHandler);
            elements.eventChannel.registerObject(eventHandler);
        }
    }

    public static void registerAll(ECModElements elements) {
        List<NetEventWrapper> nets = elements.netEvents;
        elements.warn("  Network for Event Channel {}({})", elements.eventChannelName, nets.size());
        nets.forEach(e -> e.apply(elements));
    }
}
