package site.forgus.plugins.apigenerator.normal;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class BuildMdForDubbo {

    private static NotificationGroup notificationGroup;

    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    static {
        notificationGroup = new NotificationGroup("Java2Json.NotificationGroup", NotificationDisplayType.BALLOON, true);
    }


    /**
     * 批量生成接口数据
     *
     * @param e the e
     * @return the array list
     */
    public List<FieldDocVO> generateParamFieldDocVOs(AnActionEvent e) {
        Editor editor = e.getDataContext().getData(CommonDataKeys.EDITOR);
        Project project = editor.getProject();
        String selectedText = e.getRequiredData(CommonDataKeys.EDITOR).getSelectionModel().getSelectedText();
        if (Strings.isNullOrEmpty(selectedText)) {
            Notification error = notificationGroup.createNotification("please select method or class", NotificationType.ERROR);
            Notifications.Bus.notify(error, project);
            return null;
        }
        PsiFile psiFile = e.getDataContext().getData(CommonDataKeys.PSI_FILE);
        PsiElement referenceAt = psiFile.findElementAt(editor.getCaretModel().getOffset());
        PsiClass selectedClass = (PsiClass) PsiTreeUtil.getContextOfType(referenceAt, new Class[]{PsiClass.class});
        List<FieldDocVO> fieldDocVOS = new ArrayList<>();
        if (selectedText.equals(selectedClass.getName())) {
            //TODO
        } else {
            PsiMethod[] psiMethods = selectedClass.getAllMethods();
            //寻找目标Method
            PsiMethod psiMethodTarget = null;
            for (PsiMethod psiMethod : psiMethods) {
                if (psiMethod.getName().equals(selectedText)) {
                    psiMethodTarget = psiMethod;
                    break;
                }
            }
            fieldDocVOS.addAll(listParamFieldDocVO(psiMethodTarget, project));
        }
        return fieldDocVOS;
    }

    public List<FieldDocVO> generateResponseFieldDocVOs(AnActionEvent e) {
        Editor editor = e.getDataContext().getData(CommonDataKeys.EDITOR);
        Project project = editor.getProject();
        String selectedText = e.getRequiredData(CommonDataKeys.EDITOR).getSelectionModel().getSelectedText();
        if (Strings.isNullOrEmpty(selectedText)) {
            Notification error = notificationGroup.createNotification("please select method or class", NotificationType.ERROR);
            Notifications.Bus.notify(error, project);
            return null;
        }
        PsiFile psiFile = e.getDataContext().getData(CommonDataKeys.PSI_FILE);
        PsiElement referenceAt = psiFile.findElementAt(editor.getCaretModel().getOffset());
        PsiClass selectedClass = (PsiClass) PsiTreeUtil.getContextOfType(referenceAt, new Class[]{PsiClass.class});
        List<FieldDocVO> fieldDocVOS = new ArrayList<>();
        if (selectedText.equals(selectedClass.getName())) {
            //TODO
        } else {
            PsiMethod[] psiMethods = selectedClass.getAllMethods();
            //寻找目标Method
            PsiMethod psiMethodTarget = null;
            for (PsiMethod psiMethod : psiMethods) {
                if (psiMethod.getName().equals(selectedText)) {
                    psiMethodTarget = psiMethod;
                    break;
                }
            }
            fieldDocVOS.addAll(listResponseFieldDocVO(psiMethodTarget, project));
        }
        return fieldDocVOS;
    }


    /**
     * Action performed yapi dubbo dto.
     *
     * @param psiMethodTarget the psi method target
     * @param project         the project
     * @return the yapi dubbo dto
     */
    public List<FieldDocVO> listParamFieldDocVO(PsiMethod psiMethodTarget, Project project) {
        //判断是否有匹配的目标方法
        if (psiMethodTarget == null) {
            Notification error = notificationGroup.createNotification("please check method name", NotificationType.ERROR);
            Notifications.Bus.notify(error, project);
            return null;
        }
        PsiParameter[] psiParameters = psiMethodTarget.getParameterList().getParameters();
        List<FieldDocVO> vos = new ArrayList<>();
        for (PsiParameter psiParameter : psiParameters) {
            PsiClass psiClassChild = JavaPsiFacade.getInstance(project).findClass(psiParameter.getType().getCanonicalText(), GlobalSearchScope.allScope(project));
            vos.addAll(listParamDocVO(psiClassChild));
        }
        return vos;
    }

    public List<FieldDocVO> listResponseFieldDocVO(PsiMethod psiMethodTarget, Project project) {
        //判断是否有匹配的目标方法
        if (psiMethodTarget == null) {
            Notification error = notificationGroup.createNotification("please check method name", NotificationType.ERROR);
            Notifications.Bus.notify(error, project);
            return null;
        }
        PsiType returnType = psiMethodTarget.getReturnType();
        List<FieldDocVO> vos = new ArrayList<>();
        if (returnType instanceof PsiClassReferenceType) {
            PsiClass psiClass = PsiUtil.resolveClassInType(returnType);
            vos.addAll(listParamDocVO(psiClass));
        } else {
            PsiClass psiClassChild = JavaPsiFacade.getInstance(project).findClass(returnType.getCanonicalText(), GlobalSearchScope.allScope(project));
            vos.addAll(listParamDocVO(psiClassChild));
        }
        return vos;
    }

    public static List<FieldDocVO> listParamDocVO(PsiClass psiClass) {
        return listParamDocVO(psiClass,"",false);
    }

    public static List<FieldDocVO> listParamDocVO(PsiClass psiClass,String prefix,boolean isChildParam) {
        if (psiClass == null) {
            return new ArrayList<>();
        }
        List<FieldDocVO> vos = new ArrayList<>();
        String pName = psiClass.getName();
        for (PsiField field : psiClass.getAllFields()) {
            PsiType type = field.getType();
            String newPrefix = isChildParam ? prefix + "&emsp;" : prefix;
            String fieldName = newPrefix + field.getName();
            String typeName = type.getPresentableText();
            String filedDesc = DesUtil.getFiledDesc(field.getDocComment()).replace("@see","见");
            String require = "N";
            String range = "N/A";
            PsiAnnotation[] annotations = field.getAnnotations();
            for (PsiAnnotation annotation : annotations) {

                String qualifiedName = annotation.getText();
                if(qualifiedName.contains("NotNull") || qualifiedName.contains("NotBlank")) {
                    require = "Y";
                }
                if(qualifiedName.contains("Length") || qualifiedName.contains("Range")) {
                    String min = "";
                    String max = "";
                    PsiAnnotationMemberValue maxValue = annotation.findAttributeValue("max");
                    PsiAnnotationMemberValue minValue = annotation.findAttributeValue("min");
                    if(maxValue != null) {
                        max = maxValue.getText();
                    }
                    if(minValue != null) {
                        min = "0".equals(minValue.getText()) ? "1" : minValue.getText();
                    }
                    if(StringUtils.isNotEmpty(min) && StringUtils.isNotEmpty(max)) {
                        range = "["+min + "," + max + "]";
                    }
                }
            }
            // 如果是基本类型
            if (NormalTypes.isNormalType(typeName)) {
                vos.add(FieldDocVO.normal(
                        fieldName, typeName,require, range, filedDesc
                ));
            } else if (typeName.startsWith("List")) {
                //list type
                PsiType iterableType = PsiUtil.extractIterableTypeParameter(type, false);
                PsiClass iterableClass = PsiUtil.resolveClassInClassTypeOnly(iterableType);
                String classTypeName = iterableClass.getName();
                if (NormalTypes.isNormalType(classTypeName)) {
                    vos.add(FieldDocVO.normal(
                            fieldName, classTypeName + "[]",require, range, filedDesc
                    ));
                } else {
                    vos.add(FieldDocVO.parent(fieldName, "Object[]",require, range, filedDesc));
                    vos.addAll(listParamDocVO(PsiUtil.resolveClassInType(iterableType),newPrefix,true));
                }

            }else if(typeName.contains("<")) {
                PsiClass outerClass = PsiUtil.resolveGenericsClassInType(type).getElement();
                PsiType innerType = PsiUtil.substituteTypeParameter(type, outerClass, 0, false);
                PsiClass innerClass = PsiUtil.resolveClassInClassTypeOnly(innerType);
                vos.add(FieldDocVO.parent(fieldName, "Object",require, range, filedDesc));
                vos.addAll(listParamDocVO(innerClass,newPrefix,true));
            } else {
                //class type
                PsiClass psiClass1 = PsiUtil.resolveClassInType(type);
                if (!pName.equals(psiClass1.getName())) {
                    vos.add(FieldDocVO.parent(fieldName, "Object",require, range, filedDesc));
                    vos.addAll(listParamDocVO(psiClass1,newPrefix,true));
                }else {
                    vos.add(FieldDocVO.normal(fieldName, typeName,require, range, filedDesc));
                }
            }
        }
        return vos;
    }


}

