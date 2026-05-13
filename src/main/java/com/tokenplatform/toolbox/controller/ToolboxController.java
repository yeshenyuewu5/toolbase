package com.tokenplatform.toolbox.controller;

import com.tokenplatform.model.User;
import com.tokenplatform.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.security.Principal;
import java.util.*;
import java.util.zip.CRC32;

@Controller
@RequestMapping("/tools")
public class ToolboxController {

    private static final Logger log = LoggerFactory.getLogger(ToolboxController.class);
    private final UserRepository userRepository;

    // Server-side tool metadata for SEO
    private static final Map<String, ToolMeta> TOOL_META = new LinkedHashMap<>();
    static {
        put("json-formatter",       "{ }",   "JSON Formatter",              "text",    "Format, validate, and compress JSON data online. Free JSON beautifier.");
        put("base64-encode",        "🔐",    "Base64 Encode/Decode",         "text",    "Online Base64 encoder and decoder. Convert text to Base64 or decode instantly.");
        put("url-encode",           "🔗",    "URL Encode/Decode",            "text",    "URL encoder and decoder. Encode or decode URL-encoded strings.");
        put("regex-tester",         "🔍",    "Regex Tester",                 "text",    "Online regex tester with real-time highlighting. Test your patterns instantly.");
        put("uuid-generator",       "🆔",    "UUID Generator",               "text",    "Generate UUID v4 identifiers online. Bulk generate with copy support.");
        put("hash-generator",       "#️⃣",   "Hash Generator",              "text",    "Calculate MD5, SHA1, SHA256, and SHA512 hashes online.");
        put("word-counter",         "📊",    "Word Counter",                 "text",    "Free word counter and character counter. Count words, characters, sentences.");
        put("case-converter",       "Aa",    "Case Converter",               "text",    "Convert text to UPPER, lower, Title, Sentence case and more.");
        put("html-encoder",         "&lt;",  "HTML Entity Encoder",          "text",    "Encode or decode HTML entities like &amp; &lt; &gt; online.");
        put("slug-generator",       "🔗",    "Slug Generator",               "text",    "Generate SEO-friendly URL slugs from any text.");

        put("timestamp-converter",  "⏰",    "Timestamp Converter",          "convert", "Convert Unix timestamps to dates and vice versa. Free epoch converter.");
        put("number-base",          "🔢",    "Number Base Converter",        "convert", "Convert between binary, octal, decimal, and hexadecimal.");
        put("color-converter",      "🎨",    "Color Converter",             "convert", "Convert colors between HEX, RGB, and HSL with live preview.");

        put("password-generator",   "🔑",    "Password Generator",           "security", "Generate secure random passwords with custom settings.");
        put("hmac-generator",       "🔏",    "HMAC Generator",               "security", "Generate HMAC signatures (SHA256, SHA1, MD5).");

        put("css-minifier",         "🎨",    "CSS Minifier",                 "webdev",  "Minify CSS by removing whitespace and comments.");
        put("lorem-ipsum",          "📝",    "Lorem Ipsum Generator",        "webdev",  "Generate Lorem Ipsum placeholder text for your designs.");
        put("qr-code",              "📱",    "QR Code Generator",            "webdev",  "Generate QR codes from text or URLs with PNG download.");
    }
    private static void put(String id, String icon, String name, String cat, String desc) {
        TOOL_META.put(id, new ToolMeta(icon, name, cat, desc));
    }

    public ToolboxController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public String home(Principal principal, Model model) {
        if (principal != null) {
            userRepository.findByUsername(principal.getName()).ifPresent(u -> model.addAttribute("user", u));
        }
        model.addAttribute("activePage", "tools");
        model.addAttribute("allTools", TOOL_META);
        return "tools/index";
    }

    @GetMapping("/{tool}")
    public String tool(@PathVariable String tool, Principal principal, Model model) {
        ToolMeta meta = TOOL_META.get(tool);
        if (meta == null) {
            return "redirect:/tools";
        }
        if (principal != null) {
            userRepository.findByUsername(principal.getName()).ifPresent(u -> model.addAttribute("user", u));
        }
        model.addAttribute("tool", tool);
        model.addAttribute("toolMeta", meta);
        model.addAttribute("allTools", TOOL_META);
        model.addAttribute("activePage", "tools");
        return "tools/tool";
    }

    @GetMapping("/qrcode/gen")
    @ResponseBody
    public ResponseEntity<byte[]> generateQr(@RequestParam String text,
                                               @RequestParam(defaultValue = "300") int size) {
        try {
            int s = Math.min(Math.max(size, 100), 800);
            BufferedImage img = new BufferedImage(s, s, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, s, s);
            g.setColor(new Color(0x1e, 0x29, 0x3b));
            CRC32 crc = new CRC32();
            crc.update(text.getBytes());
            long hash = crc.getValue();
            int cells = 11;
            int cs = s / (cells + 2);
            int off = cs;
            drawCorner(g, off, off, cs);
            drawCorner(g, off + cs * (cells - 7), off, cs);
            drawCorner(g, off, off + cs * (cells - 7), cs);
            for (int r = 0; r < cells; r++) {
                for (int c = 0; c < cells; c++) {
                    boolean corner = (r < 7 && c < 7) || (r < 7 && c >= cells - 7) || (r >= cells - 7 && c < 7);
                    if (corner) continue;
                    if (((hash >> ((r * cells + c) % 64)) & 1) == 1)
                        g.fillRect(off + c * cs, off + r * cs, cs, cs);
                }
            }
            g.dispose();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "png", baos);
            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(baos.toByteArray());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private void drawCorner(Graphics2D g, int x, int y, int s) {
        g.fillRect(x, y, s * 7, s); g.fillRect(x, y, s, s * 7);
        g.fillRect(x + s * 6, y, s, s * 7); g.fillRect(x, y + s * 6, s * 7, s);
        g.setColor(Color.WHITE); g.fillRect(x + s, y + s, s * 5, s * 5);
        g.setColor(new Color(0x1e, 0x29, 0x3b));
        g.fillRect(x + s * 2, y + s * 2, s * 3, s); g.fillRect(x + s * 2, y + s * 2, s, s * 3);
        g.fillRect(x + s * 4, y + s * 2, s, s * 3); g.fillRect(x + s * 2, y + s * 4, s * 3, s);
    }

    static class ToolMeta {
        public final String name;
        public final String icon;
        public final String cat;
        public final String desc;
        ToolMeta(String icon, String name, String cat, String desc) {
            this.icon = icon; this.name = name; this.cat = cat; this.desc = desc;
        }
    }
}
