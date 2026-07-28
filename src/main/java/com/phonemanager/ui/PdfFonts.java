package com.phonemanager.ui;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.BaseFont;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

// Font Unicode dùng cho PDF để hiển thị tiếng Việt có dấu.
final class PdfFonts {

    private static BaseFont regularBase;
    private static BaseFont boldBase;

    private PdfFonts() {}

    static Font regular(float size) {
        return regular(size, BaseColor.BLACK);
    }

    static Font regular(float size, BaseColor color) {
        return new Font(base(false), size, Font.NORMAL, color);
    }

    static Font bold(float size) {
        return bold(size, BaseColor.BLACK);
    }

    static Font bold(float size, BaseColor color) {
        return new Font(base(true), size, Font.BOLD, color);
    }

    private static BaseFont base(boolean bold) {
        try {
            if (bold) {
                if (boldBase == null) boldBase = BaseFont.createFont(findFont(true), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                return boldBase;
            }
            if (regularBase == null) regularBase = BaseFont.createFont(findFont(false), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            return regularBase;
        } catch (Exception e) {
            try {
                return BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
            } catch (Exception impossible) {
                throw new IllegalStateException("Không thể khởi tạo font PDF", impossible);
            }
        }
    }

    private static String findFont(boolean bold) {
        List<Path> candidates = new ArrayList<>();

        String windir = System.getenv("WINDIR");
        if (windir != null && !windir.isBlank()) {
            Path fonts = Path.of(windir, "Fonts");
            if (bold) {
                candidates.add(fonts.resolve("arialbd.ttf"));
                candidates.add(fonts.resolve("segoeuib.ttf"));
                candidates.add(fonts.resolve("tahomabd.ttf"));
            } else {
                candidates.add(fonts.resolve("arial.ttf"));
                candidates.add(fonts.resolve("segoeui.ttf"));
                candidates.add(fonts.resolve("tahoma.ttf"));
            }
        }

        if (bold) {
            candidates.add(Path.of("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"));
            candidates.add(Path.of("/System/Library/Fonts/Supplemental/Arial Bold.ttf"));
        } else {
            candidates.add(Path.of("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"));
            candidates.add(Path.of("/System/Library/Fonts/Supplemental/Arial.ttf"));
        }

        for (Path candidate : candidates) {
            if (Files.isRegularFile(candidate)) return candidate.toString();
        }
        throw new IllegalStateException("Không tìm thấy font Unicode để xuất PDF tiếng Việt");
    }
}
