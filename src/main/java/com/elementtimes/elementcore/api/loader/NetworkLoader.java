package com.elementtimes.elementcore.api.loader;

import com.elementtimes.elementcore.api.ECModElements;
import com.elementtimes.elementcore.api.ECUtils;
import com.elementtimes.elementcore.api.LoaderHelper;
import com.elementtimes.elementcore.api.annotation.ModNetwork;
import com.elementtimes.elementcore.other.NetworkObject;
import net.minecraft.network.PacketBuffer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class NetworkLoader {

    private boolean isNetworkLoaded = false;
    private ECModElements mElements;

    private List<NetworkObject> networks = new ArrayList<>();

    public NetworkLoader(ECModElements elements) {
        mElements = elements;
    }

    public List<NetworkObject> networks() {
        if (!isNetworkLoaded) {
            loadNetwork();
        }
        return networks;
    }

    private void loadNetwork() {
        LoaderHelper.stream(mElements, ModNetwork.class).forEach(data -> LoaderHelper.loadClass(mElements, data.getClassType().getClassName()).ifPresent(clazz -> {
            // boolean bEncoder = false, bDecoder = false, bConsumer = false;
            boolean[] retSign = new boolean[] {false, false, false};
            // Method encoder = null, decoder = null, consumer = null;
            Method[] ret = new Method[] {null, null, null};
            checkAndAdd(clazz, mElements, retSign, ret);
            if (!retSign[0] || !retSign[1] || !retSign[2]) {
                for (String className : LoaderHelper.getDefault(data, Collections.<String>emptyList())) {
                    Optional<Class> searchClassOpt = LoaderHelper.loadClass(mElements, className);
                    if (searchClassOpt.isPresent()) {
                        if (checkAndAdd(searchClassOpt.get(), mElements, retSign, ret)) {
                            return;
                        }
                    }
                }
            }
        }));
        isNetworkLoaded = true;
    }

    private boolean checkAndAdd(Class clazz, ECModElements elements, boolean[] signs, Method[] results) {
        checkAndAdd(clazz.getMethods(), elements, clazz, signs, results);
        if (signs[0] && signs[1] && signs[2]) {
            return true;
        }
        checkAndAdd(clazz.getDeclaredMethods(), elements, clazz, signs, results);
        return signs[0] && signs[1] && signs[2];
    }

    private void checkAndAdd(Method[] methods, ECModElements elements, Class clazz, boolean[] signs, Method[] results) {
        for (Method method : methods) {
            if (signs[0] && signs[1] && signs[2]) {
                checkMethod(method, clazz, signs, results);
                networks.add(new NetworkObject(clazz, results[0], results[1], results[2]));
                return;
            }
        }
    }

    private void checkMethod(Method method, Class clazz, boolean[] signs, Method[] results) {
        if (!signs[0]) {
            Boolean be = checkMethod(method, ModNetwork.Encoder.class, Void.class, clazz, PacketBuffer.class);
            if (Boolean.TRUE.equals(be)) {
                signs[0] = true;
            }
            if (!Boolean.FALSE.equals(be)) {
                results[0] = method;
                return;
            }
        }
        if (!signs[1]) {
            Boolean bd = checkMethod(method, ModNetwork.Decoder.class, clazz, PacketBuffer.class);
            if (Boolean.TRUE.equals(bd)) {
                signs[1] = true;
            }
            if (!Boolean.FALSE.equals(bd)) {
                results[1] = method;
                return;
            }
        }
        if (!signs[2]) {
            Boolean bc = checkMethod(method, ModNetwork.Consumer.class, Void.class, clazz, Supplier.class);
            if (Boolean.TRUE.equals(bc)) {
                signs[2] = true;
            }
            if (!Boolean.FALSE.equals(bc)) {
                results[2] = method;
            }
        }
    }

    private Boolean checkMethod(Method method, Class<? extends Annotation> annotation, Class returnType, Class... parameters) {
        if (Modifier.isStatic(method.getModifiers())) {
            if (ECUtils.reflect.checkMethodTypeAndParameters(method, returnType, parameters)) {
                return method.isAnnotationPresent(annotation) ? Boolean.TRUE : null;
            }
        }
        return Boolean.FALSE;
    }
}
