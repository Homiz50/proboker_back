package com.citynect.probroker.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.citynect.probroker.entities.MailResponse;
import com.ibm.icu.text.SimpleDateFormat;
import com.monitorjbl.xlsx.StreamingReader;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class NotificationService {

	@Autowired
	private JavaMailSender sender;

	@Transactional
	public String sendEmailsFromFile(MultipartFile file, String subject, String content)
			throws MessagingException, TemplateException {
		try {
			List<String> successEmails = new ArrayList<>();
			List<String> failureEmails = new ArrayList<>();
			Workbook workbook = StreamingReader.builder().rowCacheSize(100).bufferSize(4096)
					.open(file.getInputStream());

			for (Sheet sheet : workbook) {
				for (Row row : sheet) {
//					if (row.getRowNum() == 0)
//						continue; // Skip header row

					// Check if the row is consideredsubject empty. For simplicity, we're checking
					// if the
					// first cell is empty.
					Cell firstCell = row.getCell(0);
					if (firstCell == null || firstCell.getCellType() == CellType.BLANK) {
						// Stop processing further rows once an empty row is encountered
						break;
					}

					String email = getStringCellValue(row.getCell(0));
					String name = getStringCellValue(row.getCell(1));

					String personalizedContent = content.replace("{name}", name != null ? name : "Customer");

					System.out.println("Sending email to: " + email + " with content: " + personalizedContent);

					MailResponse response = sendEmail(personalizedContent, email, subject);
					if (response.isStatus()) {
						successEmails.add(email);
					} else {
						failureEmails.add(email);
					}
				}
			}

			StringBuilder resultMessage = new StringBuilder();
			resultMessage.append("Emails sent successfully: ").append(successEmails.size()).append("\n");
			resultMessage.append("Success Emails: ").append(successEmails).append("\n");
			resultMessage.append("Failed to send emails: ").append(failureEmails.size()).append("\n");
			resultMessage.append("Failed Emails: ").append(failureEmails);

			return resultMessage.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return "Failed to upload and save properties: " + e.getMessage();
		}
	}

	// send bulk email with customize template
	public MailResponse sendEmail(String content, String recipientEmail, String subject)
			throws MessagingException, TemplateException {
		MailResponse response = new MailResponse();
		try {
			if (!isValidEmail(recipientEmail)) {
				throw new IllegalArgumentException("Invalid email address: " + recipientEmail);
			}

			System.out.println("Preparing to send email to: " + recipientEmail);

			// Set mediaType
			MimeMessage message = sender.createMimeMessage(); // Create a new MimeMessage object for each email
			MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
					StandardCharsets.UTF_8.name());

			// Set email priority to high
			message.setHeader("X-Priority", "1");

			message.setHeader("Message-ID", "<" + UUID.randomUUID() + "@citynect.in>");

			helper.setTo(recipientEmail);
			helper.setText(content, true); // Set to true to indicate HTML content
			helper.setSubject(subject);
			helper.setFrom("flatandflatmate@citynect.in", "Team citynect");
			sender.send(message);

			response.setMessage("Mail send to: " + recipientEmail);
			response.setStatus(Boolean.TRUE);
		} catch (IOException e) {
			response.setMessage("Mail Sending failure: " + e.getMessage());
			response.setStatus(Boolean.FALSE);
		} catch (IllegalArgumentException e) {
			response.setMessage("Invalid email address: " + e.getMessage());
			response.setStatus(Boolean.FALSE);
		}
		return response;
	}

	private String getStringCellValue(Cell cell) {
		if (cell == null) {
			return ""; // Return an empty string or another appropriate default value
		}
		switch (cell.getCellType()) {
		case STRING:
			return cell.getStringCellValue();
		case NUMERIC:
			if (DateUtil.isCellDateFormatted(cell)) {
				Date date = cell.getDateCellValue();
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				return dateFormat.format(date);
			} else {
				double value = cell.getNumericCellValue();
				if (value == (long) value) // Check if the double value is equal to its long conversion
					return String.format("%d", (long) value); // Format as integer
				else
					return String.format("%s", value); // Keep as double
			}
		case BOOLEAN:
			return Boolean.toString(cell.getBooleanCellValue());
		case FORMULA:
			// For formulas, you might need to evaluate them first depending on your
			// requirements
			return cell.getCellFormula();
		default:
			return "";
		}
	}

	private boolean isValidEmail(String email) {
		// Regular expression for validating email addresses
		String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
		Pattern pattern = Pattern.compile(emailRegex);
		return pattern.matcher(email).matches();
	}

}
