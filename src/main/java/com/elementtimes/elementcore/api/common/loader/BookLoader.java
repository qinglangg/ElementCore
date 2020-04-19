package com.elementtimes.elementcore.api.common.loader;

import com.elementtimes.elementcore.api.annotation.tools.ModBook;
import com.elementtimes.elementcore.api.book.IBook;
import com.elementtimes.elementcore.api.common.ECModElements;
import com.elementtimes.elementcore.api.common.helper.ObjHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * 加载书
 * @author luqin2007
 */
public class BookLoader {

    public static List<IBook> BOOKS = new ArrayList<>();

    public static void load(ECModElements elements) {
        ObjHelper.stream(elements, ModBook.class).forEach(data -> {
            ObjHelper.find(elements, IBook.class, data).ifPresent(book -> {
                BOOKS.add(book);
                elements.warn("[Book]book: {}", book.getId());
            });
        });
    }
}
