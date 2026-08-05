package it.alterlega.recordsnext.app.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class MiniJson {
    private final String text;
    private int pos;

    private MiniJson(String text) {
        this.text = text != null && text.startsWith("\uFEFF") ? text.substring(1) : text;
    }

    static Object parse(String text) {
        MiniJson parser = new MiniJson(text);
        Object value = parser.readValue();
        parser.skipWhitespace();
        if (parser.pos != parser.text.length()) {
            throw parser.error("Contenuto inatteso dopo il valore JSON");
        }
        return value;
    }

    private Object readValue() {
        skipWhitespace();
        if (pos >= text.length()) throw error("Valore JSON mancante");
        return switch (text.charAt(pos)) {
            case '{' -> readObject();
            case '[' -> readArray();
            case '"' -> readString();
            case 't' -> readLiteral("true", Boolean.TRUE);
            case 'f' -> readLiteral("false", Boolean.FALSE);
            case 'n' -> readLiteral("null", null);
            default -> readNumber();
        };
    }

    private Map<String, Object> readObject() {
        expect('{');
        Map<String, Object> result = new LinkedHashMap<>();
        skipWhitespace();
        if (peek('}')) { pos++; return result; }
        while (true) {
            skipWhitespace();
            String key = readString();
            skipWhitespace();
            expect(':');
            result.put(key, readValue());
            skipWhitespace();
            if (peek('}')) { pos++; return result; }
            expect(',');
        }
    }

    private List<Object> readArray() {
        expect('[');
        List<Object> result = new ArrayList<>();
        skipWhitespace();
        if (peek(']')) { pos++; return result; }
        while (true) {
            result.add(readValue());
            skipWhitespace();
            if (peek(']')) { pos++; return result; }
            expect(',');
        }
    }

    private String readString() {
        expect('"');
        StringBuilder out = new StringBuilder();
        while (pos < text.length()) {
            char c = text.charAt(pos++);
            if (c == '"') return out.toString();
            if (c != '\\') { out.append(c); continue; }
            if (pos >= text.length()) throw error("Escape JSON incompleto");
            char e = text.charAt(pos++);
            switch (e) {
                case '"', '\\', '/' -> out.append(e);
                case 'b' -> out.append('\b');
                case 'f' -> out.append('\f');
                case 'n' -> out.append('\n');
                case 'r' -> out.append('\r');
                case 't' -> out.append('\t');
                case 'u' -> {
                    if (pos + 4 > text.length()) throw error("Escape Unicode incompleto");
                    out.append((char) Integer.parseInt(text.substring(pos, pos + 4), 16));
                    pos += 4;
                }
                default -> throw error("Escape JSON non valido: \\" + e);
            }
        }
        throw error("Stringa JSON non chiusa");
    }

    private Object readNumber() {
        int start = pos;
        while (pos < text.length() && "-+0123456789.eE".indexOf(text.charAt(pos)) >= 0) pos++;
        if (start == pos) throw error("Valore JSON non valido");
        String raw = text.substring(start, pos);
        try {
            return raw.contains(".") || raw.contains("e") || raw.contains("E")
                    ? Double.valueOf(raw) : Long.valueOf(raw);
        } catch (NumberFormatException ex) {
            throw error("Numero JSON non valido: " + raw);
        }
    }

    private Object readLiteral(String literal, Object value) {
        if (!text.startsWith(literal, pos)) throw error("Valore JSON non valido");
        pos += literal.length();
        return value;
    }

    private void skipWhitespace() {
        while (pos < text.length() && Character.isWhitespace(text.charAt(pos))) pos++;
    }

    private void expect(char expected) {
        skipWhitespace();
        if (pos >= text.length() || text.charAt(pos) != expected) {
            throw error("Atteso '" + expected + "'");
        }
        pos++;
    }

    private boolean peek(char c) {
        return pos < text.length() && text.charAt(pos) == c;
    }

    private IllegalArgumentException error(String message) {
        return new IllegalArgumentException(message + " alla posizione " + pos);
    }
}
