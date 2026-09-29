package com.leets7th.job_is_be.domain.mail.service;

import com.leets7th.job_is_be.domain.mail.config.MailProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class MailTemplateRenderer {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final String FONT = "Arial, 'Apple SD Gothic Neo', 'Noto Sans KR', sans-serif";

    private final MailProperties properties;

    public String welcome() {
        return welcome(null);
    }

    public String welcome(String unsubscribeToken) {
        String content = """
                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0">
                  <tr><td style="font-size:22px;font-weight:700;color:#171a1f;padding:0 0 20px">Job.is</td></tr>
                  <tr><td style="border-top:1px solid #b9bec5;font-size:1px;line-height:1px">&nbsp;</td></tr>
                  <tr><td style="padding:25px 0 0;font-size:18px;line-height:1.5;font-weight:700;color:#171a1f">Job.is에 오신 걸 환영해요.</td></tr>
                  <tr><td style="padding:13px 0 0;font-size:13px;line-height:1.8;color:#343940">무한 스크롤 대신, 매일 아침 검토를 끝낸 몇 건만 이유와 함께 보내드려요.</td></tr>
                  <tr><td style="padding:23px 0 8px;font-size:13px;font-weight:700;color:#171a1f">무엇을 받게 되나요</td></tr>
                  <tr><td style="font-size:13px;line-height:2;color:#343940">
                    <div>・ 하루 3~6건, 왜 골랐는지 이유까지</div>
                    <div>・ 관심 없으면 스킵, 마음에 들면 저장</div>
                    <div>・ 스킵과 저장이 내일 추천을 더 정확하게</div>
                  </td></tr>
                  <tr><td style="padding:23px 0 0"><div style="background:#eef0f2;border:1px solid #d8dce0;border-radius:3px;padding:18px 20px;font-size:13px;color:#343940">첫 레터는 내일 오후 6시 30분에 도착해요.</div></td></tr>
                  <tr><td style="padding:18px 0 0">%s</td></tr>
                  <tr><td style="padding:15px 0 21px;text-align:center;font-size:11px;color:#a0a6ad">수신 시간·요일은 수신 설정에서 언제든 바꿀 수 있어요.</td></tr>
                  <tr><td style="border-top:1px solid #b9bec5;font-size:1px;line-height:1px">&nbsp;</td></tr>
                  <tr><td style="padding:18px 0 0;font-size:12px;color:#858c94">곧 아침에 만나요. — Job.is 드림</td></tr>
                  <tr><td style="padding:24px 0 0">%s</td></tr>
                </table>
                """.formatted(
                button("지금 첫 레터 보기", url(properties.getWelcomePath())),
                footer(properties.getWelcomePath())
        );
        return page(content);
    }

    public String dailyBriefing(List<BriefingItem> items, String unsubscribeToken) {
        BriefingItem top = items.getFirst();
        LocalDate today = LocalDate.now(SEOUL);
        String date = today.format(DateTimeFormatter.ofPattern("yyyy. M. d."))
                + " (" + today.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN) + ") 오후 6:30";
        String skillFlow = top.skillTags().isEmpty()
                ? "추천 직무"
                : top.skillTags().stream().limit(2).map(this::escape).reduce((left, right) -> left + " · " + right).orElse("");

        StringBuilder relatedJobs = new StringBuilder();
        items.stream().skip(1).limit(3).forEach(item -> relatedJobs
                .append("<div style=\"padding:4px 0;font-size:12px;line-height:1.6;color:#343940\">・ ")
                .append(escape(item.title())).append(" - ").append(escape(item.companyName())).append("</div>"));
        if (relatedJobs.isEmpty()) {
            relatedJobs.append("<div style=\"padding:4px 0;font-size:12px;color:#858c94\">함께 추천할 공고를 준비하고 있어요.</div>");
        }

        String content = """
                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0">
                  <tr>
                    <td style="font-size:20px;font-weight:700;color:#171a1f">Job.is · 오늘의 레터</td>
                    <td align="right" style="font-size:11px;line-height:1.55;color:#343940">%s<br><span style="color:#a0a6ad">VOL.%d</span></td>
                  </tr>
                  <tr><td colspan="2" style="padding-top:17px;border-bottom:1px solid #b9bec5;font-size:1px;line-height:1px">&nbsp;</td></tr>
                  <tr><td colspan="2" style="padding:18px 0 0"><span style="display:inline-block;border:1px solid #d8dce0;border-radius:14px;padding:5px 10px;font-size:11px;color:#343940">비전공 · 백엔드 희망</span></td></tr>
                  <tr><td colspan="2" style="padding:18px 0 0;font-size:12px;line-height:1.75;color:#343940">좋은 아침입니다.<br>오늘 준비한 공고 %d건을 검토해, 당신에게 맞는 공고를 추렸습니다.<br>오늘은 원격근무 가능 공고 위주로 골랐습니다.</td></tr>
                  <tr><td colspan="2" style="padding:18px 0 0">
                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" style="border:1px solid #d8dce0;border-radius:4px">
                      <tr><td style="padding:22px 20px 0;font-size:20px;font-weight:700;color:#171a1f">%s <span style="display:inline-block;vertical-align:3px;border:1px solid #52c995;border-radius:3px;padding:2px 5px;font-size:10px;font-weight:400;color:#16845a">적합도 %d%%</span></td></tr>
                      <tr><td style="padding:13px 20px 0;font-size:11px;color:#a0a6ad">%s</td></tr>
                      <tr><td style="padding:16px 20px 0;font-size:12px;color:#343940">%s · %s · %s</td></tr>
                      <tr><td style="padding:17px 20px 0"><div style="background:#eef0f2;border-radius:3px;padding:17px 18px;font-size:12px;line-height:1.65;color:#343940">“%s”</div></td></tr>
                      <tr><td style="padding:14px 20px 20px"><div style="background:#eef0f2;border-radius:3px;padding:17px 18px;text-align:center;font-size:11px;color:#a0a6ad">%s</div></td></tr>
                    </table>
                  </td></tr>
                  <tr><td colspan="2" style="padding:20px 0 0">%s</td></tr>
                  <tr><td colspan="2" style="padding:13px 0 0;text-align:center"><a href="%s" style="font-size:11px;color:#a0a6ad;text-decoration:underline">나머지 %d건 보기 →</a></td></tr>
                  <tr><td colspan="2" style="padding:23px 0 7px;font-size:12px;font-weight:700;color:#343940">함께 추천한 공고</td></tr>
                  <tr><td colspan="2">%s</td></tr>
                  <tr><td colspan="2" style="padding-top:17px;border-bottom:1px solid #b9bec5;font-size:1px;line-height:1px">&nbsp;</td></tr>
                  <tr><td colspan="2" style="padding:18px 0 0;font-size:11px;color:#858c94">오늘도 좋은 결과 있으시길 바랍니다. — Job.is 드림</td></tr>
                  <tr><td colspan="2" style="padding:24px 0 0">%s</td></tr>
                </table>
                """.formatted(
                escape(date), today.getDayOfYear(), items.size(),
                escape(top.title()), top.fitScore(), skillFlow,
                escape(top.careerLevel()), escape(top.location()), escape(top.deadlineLabel()),
                escape(top.reason()), escape(top.companyName()) + " 채용 공고",
                button("오늘의 레터 열기", url(properties.getBriefingPath())),
                escape(url(properties.getBriefingPath())), Math.max(items.size() - 1, 0), relatedJobs,
                footer(properties.getBriefingPath())
        );
        return page(content);
    }

    private String page(String content) {
        return """
                <!doctype html>
                <html lang="ko">
                <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"></head>
                <body style="margin:0;padding:0;background:#f5f6f7;font-family:%s;color:#171a1f">
                  <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0" style="background:#f5f6f7">
                    <tr><td align="center" style="padding:72px 18px">
                      <table role="presentation" width="560" cellspacing="0" cellpadding="0" border="0" style="width:100%%;max-width:560px;background:#ffffff;border:1px solid #e2e5e8;border-radius:7px">
                        <tr><td style="padding:30px 28px 27px">%s</td></tr>
                      </table>
                    </td></tr>
                  </table>
                </body></html>
                """.formatted(FONT, content);
    }

    private String button(String label, String href) {
        return "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\"><tr>"
                + "<td align=\"center\"><a href=\"" + escape(href) + "\" style=\"display:block;max-width:290px;background:#52d39a;border-radius:5px;padding:14px 20px;color:#10271d;font-size:13px;font-weight:700;text-align:center;text-decoration:none\">"
                + escape(label) + "</a></td></tr></table>";
    }

    private String footer(String webPath) {
        String notificationSettingsUrl = url(properties.getNotificationSettingsPath());
        return """
                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" border="0">
                  <tr><td align="center" style="font-size:11px;line-height:1.7">
                    <a href="%s" style="color:#343940;text-decoration:underline">웹에서 보기</a><span style="padding:0 12px;color:#343940">·</span>
                    <a href="%s" style="color:#343940;text-decoration:underline">수신 설정</a><span style="padding:0 12px;color:#343940">·</span>
                    <a href="%s" style="color:#343940;text-decoration:underline">수신 거부</a>
                  </td></tr>
                  <tr><td align="center" style="padding:15px 0 0;font-size:10px;color:#a0a6ad">(주)잡이즈 · 서울 강남구 · help@job.is</td></tr>
                  <tr><td align="center" style="padding:17px 0 0;font-size:11px;color:#a0a6ad">Job.is</td></tr>
                </table>
                """.formatted(escape(url(webPath)), escape(notificationSettingsUrl), escape(notificationSettingsUrl));
    }

    private String url(String path) {
        String base = properties.getFrontendBaseUrl().replaceAll("/+$", "");
        return base + (path.startsWith("/") ? path : "/" + path);
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    public record BriefingItem(
            String title,
            String companyName,
            int fitScore,
            String reason,
            String careerLevel,
            String location,
            String deadlineLabel,
            List<String> skillTags
    ) {
        public BriefingItem(String title, String companyName, int fitScore, String reason) {
            this(title, companyName, fitScore, reason, "경력 무관", "지역 정보 없음", "상시", List.of());
        }

        public BriefingItem {
            skillTags = skillTags == null ? List.of() : List.copyOf(skillTags);
        }
    }
}
