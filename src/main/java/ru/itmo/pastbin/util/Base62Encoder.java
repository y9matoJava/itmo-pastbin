package ru.itmo.pastbin.util;


/**
 * Утилита для кодирования числа в строку Base62.
 *
 * Base62 использует алфавит: a-z (26) + A-Z (26) + 0-9 (10) = 62 символа.
 *
 * Примеры:
 *  encode(1) -> "b"
 *  encode(61) -> "g"
 *  encode(62) -> "ba"
 *  encode(238328) -> "baaa"
 *
 *  ДЛя чего: генерация коротких уникальых хешей для URL без коллизий.
 *  Число берется автомарного счетчика Redis (INCR),
 *  поэтому каждый хеш гарантированно уникален.
 */
public class Base62Encoder {
    /**
     * Алфаит Base62: строчные буквы + заглавные + цифры.
     * Порядок символов, определяет, какая буква соотвествует какому числу.
     */
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int BASE = ALPHABET.length(); // 62

    /**
     * кодирует число в строку Base62.
     *
     * Алгоритм аналогичен переводу числа в любую систему счисления:
     * делим на основание (62), остаток - очередной символ
     *
     * @param number число для кодирования (number > 0)
     * @return короткая строка Base62.
     */

    public static String encode(long number) {
        if (number == 0) {
            return String.valueOf(ALPHABET.charAt(0));
        }
        StringBuilder stringBuilder = new StringBuilder();
        while (number > 0) {
            // остаток от деления индекс символа в алфавите
            int remainder = (int) (number % BASE);
            stringBuilder.append(ALPHABET.charAt(remainder));
            number = number / BASE;
        }

        // разворачиваем т.к. младшие разряды добавлялись первыми
        return stringBuilder.reverse().toString();
    }
}
