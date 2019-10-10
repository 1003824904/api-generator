package site.forgus.plugins.apigenerator.config;

import com.intellij.openapi.options.Configurable;
import com.intellij.ui.components.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class ApiGeneratorSetting implements Configurable {

    /**
     * 持久化 配置
     */
    private PersistentConfig config = PersistentConfig.getInstance();

    private PersistentConfig.State state;


    JBTextField dirPathTextField;
    JBTextField prefixTextField;
    JBCheckBox cnFileNameCheckBox;

    JBTextField yApiUrlTextField;
    JBTextField tokenTextField;
    JBLabel projectIdLabel;
    JBTextField defaultCatTextField;
    JBCheckBox autoCatCheckBox;

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Api Generator Setting";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        state = config.getState();
        JBTabbedPane jbTabbedPane = new JBTabbedPane();
        GridBagLayout layout = new GridBagLayout();
        //normal setting
        JBPanel normalPanel = new JBPanel(layout);

        normalPanel.add(buildLabel(layout,"Save Directory:"));
        dirPathTextField = buildTextField(layout, state.dirPath);
        normalPanel.add(dirPathTextField);

        normalPanel.add(buildLabel(layout,"Indent Style:"));
        prefixTextField = buildTextField(layout, state.prefix);
        normalPanel.add(prefixTextField);

        cnFileNameCheckBox = buildJBCheckBox(layout,"Extract filename from doc comments",state.cnFileName);
        normalPanel.add(cnFileNameCheckBox);

        jbTabbedPane.addTab("Api Setting",normalPanel);

        //YApi setting
        JBPanel yApiPanel = new JBPanel(layout);

        yApiPanel.add(buildLabel(layout,"YApi server url:"));
        yApiUrlTextField = buildTextField(layout,state.yApiServerUrl);
        yApiPanel.add(yApiUrlTextField);

        yApiPanel.add(buildLabel(layout,"Project token:"));
        tokenTextField = buildTextField(layout,state.projectToken);
        yApiPanel.add(tokenTextField);

        yApiPanel.add(buildLabel(layout,"Project id:"));
        GridBagConstraints textConstraints = new GridBagConstraints();
        textConstraints.fill = GridBagConstraints.WEST;
        textConstraints.gridwidth = GridBagConstraints.REMAINDER;
        projectIdLabel = new JBLabel(state.projectId);
        layout.setConstraints(projectIdLabel,textConstraints);
        yApiPanel.add(projectIdLabel);

        yApiPanel.add(buildLabel(layout,"Default save category:"));
        defaultCatTextField = buildTextField(layout,state.defaultCat);
        yApiPanel.add(defaultCatTextField);

        autoCatCheckBox = buildJBCheckBox(layout,"Classify API automatically",state.autoCat);
        yApiPanel.add(autoCatCheckBox);

        jbTabbedPane.addTab("YApi Setting",yApiPanel);
        return jbTabbedPane;
    }

    private JBCheckBox buildJBCheckBox(GridBagLayout layout,String text,boolean selected) {
        JBCheckBox checkBox = new JBCheckBox();
        checkBox.setText(text);
        checkBox.setSelected(selected);
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.fill = GridBagConstraints.WEST;
        labelConstraints.gridwidth =  GridBagConstraints.REMAINDER;
        layout.setConstraints(checkBox,labelConstraints);
        return checkBox;
    }

    private JBLabel buildLabel(GridBagLayout layout,String name) {
        JBLabel jbLabel = new JBLabel(name);
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.fill = GridBagConstraints.EAST;
        labelConstraints.gridwidth = 1;
        layout.setConstraints(jbLabel,labelConstraints);
        return jbLabel;
    }

    private JBTextField buildTextField(GridBagLayout layout,String text) {
        GridBagConstraints textConstraints = new GridBagConstraints();
        textConstraints.fill = GridBagConstraints.WEST;
        textConstraints.gridwidth = GridBagConstraints.REMAINDER;
        JBTextField textField = new JBTextField(text);
        layout.setConstraints(textField,textConstraints);
        return textField;
    }

    @Override
    public boolean isModified() {
        return !state.prefix.equals(prefixTextField.getText()) ||
                state.cnFileName != cnFileNameCheckBox.isSelected() ||
                !state.yApiServerUrl.equals(yApiUrlTextField.getText()) ||
                !state.projectToken.equals(yApiUrlTextField.getText()) ||
                !state.projectId.equals(projectIdLabel.getText()) ||
                !state.defaultCat.equals(defaultCatTextField.getText()) ||
                state.autoCat != autoCatCheckBox.isSelected() ||
                !state.dirPath.equals(dirPathTextField.getText());
    }

    @Override
    public void apply() {
        config.getState().dirPath = dirPathTextField.getText();
        config.getState().prefix = prefixTextField.getText();
        config.getState().cnFileName = cnFileNameCheckBox.isSelected();
        config.getState().yApiServerUrl = yApiUrlTextField.getText();
        config.getState().projectToken = tokenTextField.getText();
        config.getState().projectId = projectIdLabel.getText();
        config.getState().defaultCat = defaultCatTextField.getText();
        config.getState().autoCat = autoCatCheckBox.isSelected();
    }

}
