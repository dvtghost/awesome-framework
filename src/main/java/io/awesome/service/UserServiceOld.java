//package io.awesome.service;
//
//import io.awesome.app.backend.dao.UserDao;
//import io.awesome.app.backend.dao.UserDaoImpl;
//import io.awesome.app.backend.enums.UserStatus;
//import io.awesome.app.backend.model.User;
//import io.awesome.app.common.enums.Role;
//import io.awesome.app.ui.views.common.models.UserUI;
//import io.awesome.dto.*;
//import io.awesome.exception.ValidateException;
//import io.awesome.ui.components.SelectDto;
//import io.awesome.util.DeepCopyBeanUtils;
//import io.awesome.util.SecurityUtil;
//import io.awesome.config.Constants;
//import org.apache.commons.lang.StringUtils;
//import org.apache.commons.validator.routines.EmailValidator;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDate;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Service
//public class UserServiceOld extends AbstractCrudService<User, UserUI.UIList, UserUI.Edit> {
//
//  private final UserDaoImpl userDaoImpl;
//  private final PropertyHelper propertyHelper;
//
//  @Autowired
//  public UserServiceOld(UserDao userDao, UserDaoImpl userDaoImpl, PropertyHelper propertyHelper) {
//    super(User.class, userDao);
//    this.userDaoImpl = userDaoImpl;
//    this.propertyHelper = propertyHelper;
//  }
//
//  @Autowired
//  public void setDao(UserDao userDao) {
//    this.dao = userDao;
//  }
//
//  @Override
//  public User mapEditEntityToModel(UserUI.Edit edit, User model) {
//    DeepCopyBeanUtils.copyProperties(edit, model, "password", "repassword");
//
//    if (edit.getMobileNumber() != null) {
//      model.setMobileNumber(String.valueOf(edit.getMobileNumber()));
//    }
//
//    if (edit.getOfficeNumber() != null) {
//      model.setOfficeNumber(String.valueOf(edit.getOfficeNumber()));
//    }
//
//    String password = edit.getPassword();
//    if (StringUtils.isBlank(edit.getId())
//        || (StringUtils.isNotBlank(edit.getId()) && StringUtils.isNotBlank(password))) {
//      model.setPassword(password);
//      model.setRepassword(edit.getRepassword());
//      model = hashPassword(model);
//    }
//    if (StringUtils.isBlank(model.getId())) {
//      model.setCreatingNewObject(true);
//    }
//    return model;
//  }
//
//  @Override
//  protected User preSave(User model) {
//    if (model.getCreatingNewObject()) {
//      model.setFirstTimeLoggedIn(true);
//    }
//    return super.preSave(model);
//  }
//
//  public void updatePassword(UserUI.PasswordManagementUI edit) {
//    ErrorsDto errorsDto = new ErrorsDto();
//    if (StringUtils.isBlank(edit.getPassword())) {
//      errorsDto.add(new ErrorDto("password", "This value is required."));
//    } else {
//      if (!edit.getPassword().matches(Constants.PASSWORD_PATTERN)) {
//        errorsDto.add(
//            new ErrorDto(
//                "password",
//                "A password must be at least 8 characters. It has to have at least one symbol, one letter and one numeric."));
//      } else {
//        if (!edit.getConfirmPassword().equals(edit.getPassword())) {
//          errorsDto.add(new ErrorDto("confirmPassword", "Retype Password should like password."));
//        }
//      }
//    }
//
//    if (errorsDto.hasErrors()) {
//      throw new ValidateException(errorsDto);
//    }
//
//    UserSessionDto userSessionDto =
//        (UserSessionDto) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//
//    var data = dao.findById(userSessionDto.getUserId());
//
//    if (data.isPresent()) {
//      var user = data.get();
//      user.setPassword(edit.getPassword());
//      user = hashPassword(user);
//      user.setFirstTimeLoggedIn(false);
//      dao.save(user);
//    }
//  }
//
//  @Override
//  protected List<String> getIgnoreProperties() {
//    return Collections.singletonList("password");
//  }
//
//  @Override
//  protected void postCopyEntityToUIList(User model, UserUI.UIList listItem) {
//    super.postCopyEntityToUIList(model, listItem);
//    listItem.setName(model.fullName());
//    if (model.getUserStatus() != null) {
//      listItem.setUserStatus(model.getUserStatus().getLabel());
//    }
//  }
//
//  @Override
//  protected void postCopyEntityToUIEdit(User model, UserUI.Edit edit) {
//    super.postCopyEntityToUIEdit(model, edit);
//    if (StringUtils.isNotBlank(model.getMobileNumber())) {
//      edit.setMobileNumber(Long.parseLong(model.getMobileNumber()));
//    }
//
//    if (StringUtils.isNotBlank(model.getOfficeNumber())) {
//      edit.setOfficeNumber(Long.parseLong(model.getOfficeNumber()));
//    }
//  }
//
//  @Override
//  public Supplier<UserUI.UIList> getListableSupplier() {
//    return UserUI.UIList::new;
//  }
//
//  @Override
//  public Supplier<UserUI.Edit> getEditableSupplier() {
//    return UserUI.Edit::new;
//  }
//
//  public List<SelectDto.SelectItem> buildAllUserSelectItems(List<User> users) {
//    return users.stream()
//        .map(user -> new SelectDto.SelectItem(user.getId(), user.fullName()))
//        .collect(Collectors.toList());
//  }
//
//  public List<User> findAll() {
//    List<User> users = (List<User>) dao.findAll();
//    return users.stream()
//        .filter(user -> UserStatus.Enable.equals(user.getUserStatus()))
//        .collect(Collectors.toList());
//  }
//
//  public Iterable<User> findAllById(List<String> ids) {
//    return dao.findAllById(ids);
//  }
//
//  public List<User> findByRoleIsIn(Role... roles) {
//    List<FilterDto> filters = new ArrayList<>();
//    filters.add(
//        new FilterDto(
//            "role", FilterDto.Operator.IN, Arrays.stream(roles).map(Role::getValue).toArray()));
//    return getRecordPerPage(Pageable.unpaged(), filters).getResults();
//  }
//
//  public List<UserDto> retrieveResourcesForScheduling(UserStatus userStatus, Role... roles) {
//    List<User> result = ((UserDao) dao).findByUserStatusAndRoleIn(userStatus, Arrays.asList(roles));
//    return result.stream().map(UserDto::convertToDto).collect(Collectors.toList());
//  }
//
//  @Override
//  protected ErrorsDto validateForSave(UserUI.Edit edit, ErrorsDto errorsDto) {
//    if (StringUtils.isBlank(edit.getFirstName())) {
//      errorsDto.add(new ErrorDto("firstName", "This value is required."));
//    }
//
//    if (StringUtils.isBlank(edit.getLastName())) {
//      errorsDto.add(new ErrorDto("lastName", "This value is required."));
//    }
//
//    if (StringUtils.isBlank(edit.getEmployeeId())) {
//      errorsDto.add(new ErrorDto("employeeId", "This value is required."));
//    } else if (existedByFieldAndId(edit.getId(), "employeeId", edit.getEmployeeId())) {
//      errorsDto.add(new ErrorDto("employeeId", "Employee Id is used."));
//    }
//
//    if (StringUtils.isBlank(edit.getEmail())) {
//      errorsDto.add(new ErrorDto("email", "This value is required."));
//    }
//
//    if (StringUtils.isNotBlank(edit.getEmail())) {
//      if (!EmailValidator.getInstance().isValid(edit.getEmail())) {
//        errorsDto.add(new ErrorDto("email", "Email address is invalid."));
//      } else if (existedByFieldAndId(edit.getId(), "email", edit.getEmail())) {
//        errorsDto.add(new ErrorDto("email", "Email is used."));
//      }
//    }
//
//    if (!edit.getRole().hasSelectedItems()) {
//      errorsDto.add(new ErrorDto("role", "This value is required."));
//    }
//
//    if (StringUtils.isBlank(edit.getId())) {
//      if (StringUtils.isBlank(edit.getPassword())) {
//        errorsDto.add(new ErrorDto("password", "Password is required."));
//      }
//    }
//
//    if (StringUtils.isNotBlank(edit.getPassword())) {
//      if (!edit.getPassword().matches(Constants.PASSWORD_PATTERN)) {
//        errorsDto.add(
//            new ErrorDto(
//                "password",
//                "A password must be at least 8 characters. It has to have at least one symbol, one letter and one numeric."));
//      } else if (!edit.getRepassword().equals(edit.getPassword())) {
//        errorsDto.add(new ErrorDto("repassword", "Retype Password should like password."));
//      }
//    }
//
//    return super.validateForSave(edit, errorsDto);
//  }
//
//  public boolean existedByFieldAndId(String id, String fieldName, Object value) {
//    List<FilterDto> filters = new ArrayList<>();
//    if (StringUtils.isNotBlank(id)) {
//      filters.add(new FilterDto("id", FilterDto.Operator.NE, new Object[] {id}));
//    }
//    filters.add(new FilterDto(fieldName, FilterDto.Operator.EQ, new Object[] {value}));
//    return !getRecordPerPage(PageRequest.of(1, 1), filters).getResults().isEmpty();
//  }
//
//  public User createResource(String employeeId) {
//    User user = new User();
//    user.setEmployeeId(employeeId);
//    user.setNeedToRehashPassword(true);
//    user.setPassword(propertyHelper.userDefaultPassword);
//    user.setRepassword(propertyHelper.userDefaultPassword);
//    user.setRole(Role.TherapyAssistant);
//    return saveWithoutValidation(user);
//  }
//
//  public User saveWithoutValidation(User model) {
//    model = preSave(model);
//    model = dao.save(model);
//    model = postSave(model);
//    return model;
//  }
//
//  public Optional<User> findById(String id) {
//    return dao.findById(id);
//  }
//
//  public Optional<User> findByEmail(String email) {
//    return ((UserDao) dao).findByEmail(email);
//  }
//
//  public User hashPassword(User user) {
//    String hashSalt = getHashSalt();
//    user.setHashSalt(hashSalt);
//    user.setPassword(
//        encodePassword(SecurityUtil.getInstance().firstLevelEncode(user.getPassword(), hashSalt)));
//    user.setPasswordChangedDate(LocalDate.now());
//    return user;
//  }
//
//  private String getHashSalt() {
//    return UUID.randomUUID().toString();
//  }
//
//  public boolean verifyPassword(String rawPassword, String hashSalt, String hashedPassword) {
//    BCryptPasswordEncoder encoder = getbCryptPasswordEncoder();
//    return encoder.matches(
//        SecurityUtil.getInstance().firstLevelEncode(rawPassword, hashSalt), hashedPassword);
//  }
//
//  private BCryptPasswordEncoder getbCryptPasswordEncoder() {
//    return new BCryptPasswordEncoder(BCryptPasswordEncoder.BCryptVersion.$2Y, 8);
//  }
//
//  public String encodePassword(String rawPassword) {
//    return getbCryptPasswordEncoder().encode(rawPassword);
//  }
//
//  public List<User> findUserNeedResetPassword() {
//    PagingDto<User> paging =
//        new PagingDto<>(
//            Pageable.unpaged(),
//            Collections.singletonList(
//                new FilterDto(
//                    "passwordChangedDate",
//                    FilterDto.Operator.BEFORE,
//                    new Object[] {LocalDate.now().minusYears(1)})),
//            new ArrayList<>());
//    paging = dao.searchByFilters(User.class, paging, true);
//    return paging.getResults();
//  }
//}
