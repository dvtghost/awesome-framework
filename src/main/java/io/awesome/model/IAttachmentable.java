package io.awesome.model;

import com.vaadin.flow.component.upload.receivers.FileBuffer;

public interface IAttachmentable {

  long UPLOAD_MAX_FILESIZE = 1048576;

  String UPLOAD_ALLOWED_EXTENSIONS = ".jpg, .jpeg, .gif, .png, .xlsx, .xls, .doc, .docx, pdf";

  String getId();

  FileBuffer getAttachment();

  void setAttachment(FileBuffer attachment);
}
