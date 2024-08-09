package cn.octautumn.csuemailclient.common;

import lombok.Getter;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class Mail
{
    @Getter
    private final int index;  // 邮件编号
    @Getter
    private final String subject;  // 邮件主题
    @Getter
    private final String sender;  // 发件人
    @Getter
    private final String receiver;  // 收件人
    @Getter
    private final String date;  // 发件日期
    @Getter
    private final boolean isContainAttachment;  // 是否有附件
    @Getter
    private final String content;  // 邮件正文private
    @Getter
    private final HashMap<String, BodyPart> attachFiles = new HashMap<>();  // 附件，以键值对的形式来存储文件

    private boolean hasHtml = false;

    public int getIndex()
    {
        return this.index;
    }

    /**
     * 构造函数，对传入的邮件进行解析
     *
     * @param message 邮件
     * @throws MessagingException 异常
     * @throws IOException        异常
     */
    public Mail(Message message, int index) throws MessagingException, IOException
    {
        this.index = index;
        MimeMessage mm = (MimeMessage) message;
        // 解析邮件主题
        this.subject = parseTopic(mm);
        // 解析发件人
        this.sender = parseSender(mm);
        // 解析收件人
        this.receiver = parseReceiver(mm);
        // 解析发送时间
        this.date = parseDate(mm);
        // 判断是否有附件
        this.isContainAttachment = isAttached(mm);
        // 解析正文
        StringBuilder sb = new StringBuilder(30);
        parseContent(mm, sb);
        this.content = sb.toString();
        // 如果有附件的话就去解析附件
        if (isContainAttachment)
        {
            parseAttachment(mm);
        }
    }

    /**
     * 解析邮件主题
     *
     * @param mm 邮件
     * @return 邮件主题
     * @throws MessagingException           异常
     * @throws UnsupportedEncodingException 异常
     */
    private String parseTopic(MimeMessage mm) throws MessagingException, UnsupportedEncodingException
    {
        return MimeUtility.decodeText(mm.getSubject());
    }

    /**
     * 解析发件人
     *
     * @param mm 邮件
     * @return 发件人
     * @throws UnsupportedEncodingException 异常
     * @throws MessagingException           异常
     */
    private String parseSender(MimeMessage mm) throws UnsupportedEncodingException, MessagingException
    {
        Address[] froms = mm.getFrom();
        String person = "";
        String fromAddress = "";
        if (froms != null)
        {
            InternetAddress address = (InternetAddress) froms[0];
            fromAddress = address.getAddress();
            person = address.getPersonal();
            if (person != null)
            {
                person = MimeUtility.decodeText(person);
            } else
                return "<" + fromAddress + ">";
        }
        return person + "<" + fromAddress + ">";
    }

    /**
     * 解析收件人
     *
     * @param mm 邮件
     * @return 返回收件人
     * @throws MessagingException 异常
     */
    private String parseReceiver(MimeMessage mm) throws MessagingException, UnsupportedEncodingException
    {
        Address[] to = mm.getAllRecipients();
        StringBuilder toAddress = new StringBuilder();
        if (to != null)
        {
            for (Address address : to)
            {
                System.out.println();
                InternetAddress internetAddress = (InternetAddress) address;
                String fromAddress = internetAddress.getAddress();
                String person = internetAddress.getPersonal();
                if (person != null)
                {
                    person = MimeUtility.decodeText(person);
                    toAddress.append(person);
                }
                toAddress.append("<").append(fromAddress).append(">, ");
            }
            toAddress.deleteCharAt(toAddress.length() - 1);  // 删掉最后一个逗号
        }
        return toAddress.toString();
    }

    /**
     * 解析邮件发送时间
     *
     * @param mm 邮件
     * @return 发送时间
     * @throws MessagingException 异常
     */
    private String parseDate(MimeMessage mm) throws MessagingException
    {
        String sendDate = "";
        Date date = mm.getSentDate();
        if (date != null)
        {
            sendDate = new SimpleDateFormat("yyyy年MM月dd日 E HH:mm ").format(date);
        }
        return sendDate;
    }

    /**
     * 解析邮件是否含有附件
     *
     * @param part 邮件
     * @return 布尔值
     * @throws MessagingException 异常
     * @throws IOException        异常
     */
    private boolean isAttached(Part part) throws MessagingException, IOException
    {
        boolean flag = false;
        if (part.isMimeType("multipart/*"))
        {
            MimeMultipart multipart = (MimeMultipart) part.getContent();
            int partCount = multipart.getCount();
            for (int i = 0; i < partCount; i++)
            {
                BodyPart bodyPart = multipart.getBodyPart(i);
                String disp = bodyPart.getDisposition();
                if (disp != null && (disp.equalsIgnoreCase(Part.ATTACHMENT) || disp.equalsIgnoreCase(Part.INLINE)))
                {
                    flag = true;
                } else if (bodyPart.isMimeType("multipart/*"))
                {
                    flag = isAttached(bodyPart);
                } else
                {
                    String contentType = bodyPart.getContentType();
                    if (contentType.contains("application"))
                    {
                        flag = true;
                    }
                    if (contentType.contains("name"))
                    {
                        flag = true;
                    }
                }
                if (flag) break;
            }
        } else if (part.isMimeType("message/rfc822"))
        {
            flag = isAttached((Part) part.getContent());
        }
        return flag;
    }

    /**
     * 解析邮件文本内容
     *
     * @param part    邮件体
     * @param content 存储邮件文本内容的字符串
     * @throws MessagingException 异常
     * @throws IOException        异常
     */
    public void parseContent(Part part, StringBuilder content) throws MessagingException, IOException
    {
        //如果是文本类型的附件，通过getContent方法可以取到文本内容，但这不是我们需要的结果，所以在这里要做判断
        boolean isContainTextAttach = part.getContentType().indexOf("name") > 0;
        if (part.isMimeType("text/plain") && !isContainTextAttach)
        {
            content.append(part.getContent().toString());
        } else if (part.isMimeType("text/html") && !isContainTextAttach)
        {
            content.append(part.getContent().toString());
            hasHtml = true;
        } else if (part.isMimeType("message/rfc822"))
        {
            parseContent((Part) part.getContent(), content);
        } else if (part.isMimeType("multipart/*"))
        {
            Multipart multipart = (Multipart) part.getContent();
            for (int i = 0; i < multipart.getCount(); i++)
                parseContent(multipart.getBodyPart(i), content);
        }
    }

    /**
     * 解析附件
     *
     * @param part 邮件中多个组合体中的其中一个组合体
     * @throws UnsupportedEncodingException 嗯
     * @throws MessagingException           嘛
     * @throws IOException                  啊
     */
    public void parseAttachment(Part part) throws UnsupportedEncodingException, MessagingException,
            IOException
    {
        if (part.isMimeType("multipart/*"))
        {
            Multipart multipart = (Multipart) part.getContent();    //复杂体邮件
            //复杂体邮件包含多个邮件体
            int partCount = multipart.getCount();
            for (int i = 0; i < partCount; i++)
            {
                //获得复杂体邮件中其中一个邮件体
                BodyPart bodyPart = multipart.getBodyPart(i);
                //某一个邮件体也有可能是由多个邮件体组成的复杂体
                String disp = bodyPart.getDisposition();
                if (disp != null && (disp.equalsIgnoreCase(Part.ATTACHMENT) || disp.equalsIgnoreCase(Part.INLINE)))
                {
                    saveFile(bodyPart);
                } else if (bodyPart.isMimeType("multipart/*"))
                {
                    parseAttachment(bodyPart);
                } else
                {
                    String contentType = bodyPart.getContentType();
                    if (contentType.contains("name") || contentType.contains("application"))
                    {
                        saveFile(bodyPart);
                    }
                }
            }
        } else if (part.isMimeType("message/rfc822"))
        {
            parseAttachment((Part) part.getContent());
        }
    }

    /**
     * 将附件保存下来
     *
     * @param part 附件
     */
    public void saveFile(BodyPart part) throws MessagingException, UnsupportedEncodingException
    {
        attachFiles.put(decodeText(part.getFileName()), part);
    }

    /**
     * 文本解码
     *
     * @param encodeText 解码MimeUtility.encodeText(String text)方法编码后的文本
     * @return 解码后的文本
     * @throws UnsupportedEncodingException 异常
     */
    public String decodeText(String encodeText) throws UnsupportedEncodingException
    {
        if (encodeText == null || encodeText.equals(""))
        {
            return "";
        } else
        {
            return MimeUtility.decodeText(encodeText);
        }
    }
}