(function () {
  const PROFILE_API = "/api/blog/profile";
  const BLOG_LOGIN_API = "/api/blog/admin/login";
  const BLOG_SAVE_API = "/api/blog/admin/profile";
  const BLOG_TOKEN_KEY = "sqh.blog.admin.token";

  const store = window.SqhSiteStore;
  const state = {
    site: store.loadSite()
  };

  const elements = {
    brandMark: document.getElementById("brand-mark"),
    siteName: document.getElementById("site-name"),
    siteTagline: document.getElementById("site-tagline"),
    profileName: document.getElementById("profile-name"),
    avatarImage: document.getElementById("avatar-image"),
    avatarFallback: document.getElementById("avatar-fallback"),
    profileLines: document.getElementById("profile-lines"),
    skillsList: document.getElementById("skills-list"),
    projectsList: document.getElementById("projects-list"),
    updatesList: document.getElementById("updates-list"),
    contactsList: document.getElementById("contacts-list"),
    siteFooter: document.getElementById("site-footer"),
    editorPane: document.getElementById("editor-pane"),
    toggleEditor: document.getElementById("toggle-editor"),
    saveSite: document.getElementById("save-site"),
    resetSite: document.getElementById("reset-site"),
    siteNameInput: document.getElementById("site-name-input"),
    brandInput: document.getElementById("brand-input"),
    taglineInput: document.getElementById("tagline-input"),
    profileNameInput: document.getElementById("profile-name-input"),
    avatarTextInput: document.getElementById("avatar-text-input"),
    avatarImageInput: document.getElementById("avatar-image-input"),
    avatarFileInput: document.getElementById("avatar-file-input"),
    footerInput: document.getElementById("footer-input"),
    profileLinesEditor: document.getElementById("profile-lines-editor"),
    skillsEditor: document.getElementById("skills-editor"),
    projectsEditor: document.getElementById("projects-editor"),
    updatesEditor: document.getElementById("updates-editor"),
    contactsEditor: document.getElementById("contacts-editor"),
    addProfileLine: document.getElementById("add-profile-line"),
    addSkill: document.getElementById("add-skill"),
    addProject: document.getElementById("add-project"),
    addUpdate: document.getElementById("add-update"),
    addContact: document.getElementById("add-contact")
  };

  function unwrapResult(payload) {
    if (payload && Object.prototype.hasOwnProperty.call(payload, "data")) {
      return payload.data;
    }
    return payload;
  }

  function normalizeSkill(skill) {
    if (typeof skill === "string") {
      return { id: "skill-" + skill, label: skill, noteTag: skill, link: "" };
    }
    return skill || store.createSkill();
  }

  function mapProfileToSite(profile) {
    if (!profile) {
      return store.loadSite();
    }
    return {
      siteName: profile.siteName || "Sqh的灵感仓库",
      brand: profile.brand || "Sqh's Pixel Lab",
      tagline: profile.heroTagline || "",
      profileName: profile.profileName || "",
      avatarText: profile.avatar || "SQH",
      avatarImage: "",
      profileLines: [profile.profileLine1, profile.profileLine2, profile.profileLine3].filter(Boolean),
      skills: (profile.skills || []).map(normalizeSkill),
      projects: (profile.projects || []).map(function (project, index) {
        return {
          id: "project-" + index,
          title: project.title || "未命名项目",
          description: project.description || "",
          image: project.imageUrl || "",
          link: project.href || "",
          tags: project.tags || []
        };
      }),
      updates: profile.updates || [],
      contacts: profile.contacts || [],
      footer: profile.footerText || "个人工坊 · 灵感笔记"
    };
  }

  function mapSiteToProfile(site) {
    return {
      siteName: site.siteName,
      brand: site.brand,
      heroTitle: site.siteName,
      heroTagline: site.tagline,
      avatar: site.avatarText || "SQH",
      profileName: site.profileName,
      profileLine1: site.profileLines[0] || "",
      profileLine2: site.profileLines[1] || "",
      profileLine3: site.profileLines[2] || "",
      skills: site.skills.map(function (skill) { return skill.label; }),
      updates: site.updates,
      contacts: site.contacts,
      projects: site.projects.map(function (project) {
        return {
          title: project.title,
          description: project.description,
          imageUrl: project.image,
          href: project.link,
          tags: project.tags
        };
      }),
      footerText: "个人工坊 · 灵感笔记"
    };
  }

  function requestJson(url, options) {
    return fetch(url, options).then(function (response) {
      return response.text().then(function (text) {
        var payload = null;
        if (text) {
          try {
            payload = JSON.parse(text);
          } catch (error) {
            throw new Error("服务器返回格式异常");
          }
        }
        if (response.status === 401 || response.status === 403) {
          localStorage.removeItem(BLOG_TOKEN_KEY);
          throw new Error("登录已过期或无权限，请重新输入管理密码");
        }
        if (!response.ok || (payload && payload.code && payload.code !== 200)) {
          throw new Error((payload && payload.message) || "请求失败");
        }
        if (!payload) {
          throw new Error("服务器未返回数据");
        }
        return unwrapResult(payload);
      });
    });
  }

  function loadRemoteProfile(force) {
    if (!force && store.hasLocalSite()) {
      return Promise.resolve();
    }
    const wasEditing = !elements.editorPane.hidden;
    return requestJson(PROFILE_API).then(function (profile) {
      state.site = mapProfileToSite(profile);
      renderProfile();
      renderEditor();
      elements.editorPane.hidden = !wasEditing;
    }).catch(function (error) {
      console.warn("load blog profile failed", error);
    });
  }

  function getAdminToken() {
    const existing = localStorage.getItem(BLOG_TOKEN_KEY);
    if (existing) {
      return Promise.resolve(existing);
    }
    const password = window.prompt("请输入个人主页管理密码");
    if (!password) {
      return Promise.reject(new Error("password required"));
    }
    return requestJson(BLOG_LOGIN_API, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ password: password })
    }).then(function (result) {
      localStorage.setItem(BLOG_TOKEN_KEY, result.token);
      return result.token;
    });
  }

  function saveRemoteProfile() {
    return getAdminToken().then(function (token) {
      return requestJson(BLOG_SAVE_API, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: "Bearer " + token
        },
        body: JSON.stringify(mapSiteToProfile(state.site))
      });
    }).catch(function (error) {
      if (String(error.message || "").indexOf("forbidden") >= 0) {
        localStorage.removeItem(BLOG_TOKEN_KEY);
      }
      throw error;
    });
  }

  function createTextRow(list, onRemove) {
    const row = document.createElement("div");
    row.className = "row-editor";

    const input = document.createElement("input");
    input.type = "text";
    input.value = list;

    const button = document.createElement("button");
    button.type = "button";
    button.className = "pixel-button danger";
    button.textContent = "删除";
    button.addEventListener("click", onRemove);

    row.appendChild(input);
    row.appendChild(button);
    return { row: row, input: input };
  }

  function renderProfile() {
    const site = state.site;
    elements.brandMark.textContent = site.brand;
    elements.siteName.textContent = site.siteName;
    elements.siteTagline.textContent = site.tagline;
    elements.profileName.textContent = site.profileName;
    elements.siteFooter.innerHTML =
      '<div>个人工坊 · 灵感笔记</div>' +
      '<div style="margin-top: 12px;">' +
      '<a href="https://beian.miit.gov.cn" target="_blank" rel="noopener noreferrer">辽ICP备2026008235号-2</a>' +
      "</div>";

    if (site.avatarImage) {
      elements.avatarImage.src = site.avatarImage;
      elements.avatarImage.hidden = false;
      elements.avatarFallback.hidden = true;
    } else {
      elements.avatarImage.hidden = true;
      elements.avatarFallback.hidden = false;
      elements.avatarFallback.textContent = site.avatarText || "SQH";
    }

    elements.profileLines.innerHTML = "";
    site.profileLines.forEach(function (line) {
      const p = document.createElement("div");
      p.textContent = line;
      elements.profileLines.appendChild(p);
    });

    elements.skillsList.innerHTML = "";
    site.skills.forEach(function (skill) {
      const a = document.createElement("a");
      a.className = "chip-link";
      a.href = store.buildSkillHref(skill);
      a.textContent = skill.label;
      elements.skillsList.appendChild(a);
    });

    elements.projectsList.innerHTML = "";
    site.projects.forEach(function (project) {
      const article = document.createElement(project.link ? "a" : "article");
      article.className = "project-card";
      if (project.link) {
        article.href = project.link;
        article.target = project.link.indexOf("http") === 0 ? "_blank" : "_self";
        article.rel = "noopener noreferrer";
      }

      const media = document.createElement("div");
      media.className = "project-media";
      if (project.image) {
        const img = document.createElement("img");
        img.src = project.image;
        img.alt = project.title;
        media.appendChild(img);
      } else {
        media.textContent = "PROJECT";
      }

      const body = document.createElement("div");
      body.className = "project-body";

      const title = document.createElement("h3");
      title.className = "project-title";
      title.textContent = project.title;

      const desc = document.createElement("p");
      desc.className = "project-desc";
      desc.textContent = project.description;

      const tags = document.createElement("div");
      tags.className = "tag-row";
      project.tags.forEach(function (tag) {
        const chip = document.createElement("span");
        chip.className = "tag-chip";
        chip.textContent = tag;
        tags.appendChild(chip);
      });

      const link = document.createElement("a");
      link.className = "pixel-button project-link";
      link.href = project.link || "#";
      link.textContent = "进入项目";
      link.addEventListener("click", function (event) {
        event.stopPropagation();
      });
      if (!project.link) {
        link.addEventListener("click", function (event) {
          event.preventDefault();
        });
      }

      body.appendChild(title);
      body.appendChild(tags);
      body.appendChild(desc);
      body.appendChild(link);
      article.appendChild(media);
      article.appendChild(body);
      elements.projectsList.appendChild(article);
    });

    renderTextList(elements.updatesList, site.updates);
    renderTextList(elements.contactsList, site.contacts);
  }

  function renderTextList(container, list) {
    container.innerHTML = "";
    list.forEach(function (item) {
      const row = document.createElement("div");
      row.textContent = item;
      container.appendChild(row);
    });
  }

  function bindInputs() {
    elements.siteNameInput.value = state.site.siteName;
    elements.brandInput.value = state.site.brand;
    elements.taglineInput.value = state.site.tagline;
    elements.profileNameInput.value = state.site.profileName;
    elements.avatarTextInput.value = state.site.avatarText;
    elements.avatarImageInput.value = state.site.avatarImage;
    elements.footerInput.value = state.site.footer;
  }

  function renderEditor() {
    bindInputs();
    renderTextEditor(elements.profileLinesEditor, state.site.profileLines, "profileLines");
    renderSkillEditor();
    renderProjectEditor();
    renderTextEditor(elements.updatesEditor, state.site.updates, "updates");
    renderTextEditor(elements.contactsEditor, state.site.contacts, "contacts");
  }

  function renderTextEditor(container, list, key) {
    container.innerHTML = "";
    list.forEach(function (line, index) {
      const item = createTextRow(line, function () {
        state.site[key].splice(index, 1);
        renderEditor();
      });
      item.input.addEventListener("input", function () {
        state.site[key][index] = item.input.value;
        renderProfile();
      });
      container.appendChild(item.row);
    });
  }

  function renderSkillEditor() {
    elements.skillsEditor.innerHTML = "";
    state.site.skills.forEach(function (skill, index) {
      const block = document.createElement("div");
      block.className = "row-editor";

      const label = document.createElement("input");
      label.type = "text";
      label.value = skill.label;
      label.placeholder = "技能名称";

      const noteTag = document.createElement("input");
      noteTag.type = "text";
      noteTag.value = skill.noteTag;
      noteTag.placeholder = "关联笔记标签";

      const link = document.createElement("input");
      link.type = "text";
      link.value = skill.link;
      link.placeholder = "自定义链接，可留空";

      const remove = document.createElement("button");
      remove.type = "button";
      remove.className = "pixel-button danger";
      remove.textContent = "删除";

      label.addEventListener("input", function () {
        state.site.skills[index].label = label.value;
        renderProfile();
      });
      noteTag.addEventListener("input", function () {
        state.site.skills[index].noteTag = noteTag.value;
        renderProfile();
      });
      link.addEventListener("input", function () {
        state.site.skills[index].link = link.value;
        renderProfile();
      });
      remove.addEventListener("click", function () {
        state.site.skills.splice(index, 1);
        renderEditor();
        renderProfile();
      });

      block.appendChild(label);
      block.appendChild(noteTag);
      block.appendChild(link);
      block.appendChild(remove);
      elements.skillsEditor.appendChild(block);
    });
  }

  function renderProjectEditor() {
    elements.projectsEditor.innerHTML = "";
    state.site.projects.forEach(function (project, index) {
      const block = document.createElement("div");
      block.className = "project-editor";

      const title = document.createElement("input");
      title.type = "text";
      title.value = project.title;
      title.placeholder = "项目标题";

      const link = document.createElement("input");
      link.type = "text";
      link.value = project.link;
      link.placeholder = "项目链接";

      const tags = document.createElement("input");
      tags.type = "text";
      tags.value = project.tags.join(", ");
      tags.placeholder = "技术栈标签，逗号分隔";

      const desc = document.createElement("textarea");
      desc.rows = 3;
      desc.value = project.description;
      desc.placeholder = "项目描述";

      const image = document.createElement("input");
      image.type = "text";
      image.value = project.image;
      image.placeholder = "项目图片链接";

      const file = document.createElement("input");
      file.type = "file";
      file.accept = "image/*";

      const preview = document.createElement("div");
      preview.className = "project-thumb";
      if (project.image) {
        const img = document.createElement("img");
        img.src = project.image;
        img.alt = project.title;
        preview.appendChild(img);
      } else {
        preview.textContent = "图片预览";
      }

      const remove = document.createElement("button");
      remove.type = "button";
      remove.className = "pixel-button danger";
      remove.textContent = "删除项目";

      title.addEventListener("input", function () {
        state.site.projects[index].title = title.value;
        renderProfile();
      });
      link.addEventListener("input", function () {
        state.site.projects[index].link = link.value;
        renderProfile();
      });
      tags.addEventListener("input", function () {
        state.site.projects[index].tags = splitText(tags.value);
        renderProfile();
      });
      desc.addEventListener("input", function () {
        state.site.projects[index].description = desc.value;
        renderProfile();
      });
      image.addEventListener("input", function () {
        state.site.projects[index].image = image.value.trim();
        renderEditor();
        renderProfile();
      });
      file.addEventListener("change", function () {
        const selected = file.files && file.files[0];
        if (!selected) {
          return;
        }
        store.readFileAsDataUrl(selected).then(function (result) {
          state.site.projects[index].image = result;
          renderEditor();
          renderProfile();
        });
      });
      remove.addEventListener("click", function () {
        state.site.projects.splice(index, 1);
        renderEditor();
        renderProfile();
      });

      block.appendChild(preview);
      block.appendChild(title);
      block.appendChild(link);
      block.appendChild(tags);
      block.appendChild(desc);
      block.appendChild(image);
      block.appendChild(file);
      block.appendChild(remove);
      elements.projectsEditor.appendChild(block);
    });
  }

  function splitText(value) {
    return value.split(/[，,]/).map(function (item) {
      return item.trim();
    }).filter(Boolean);
  }

  function syncFromInputs() {
    state.site.siteName = elements.siteNameInput.value.trim();
    state.site.brand = elements.brandInput.value.trim();
    state.site.tagline = elements.taglineInput.value.trim();
    state.site.profileName = elements.profileNameInput.value.trim();
    state.site.avatarText = elements.avatarTextInput.value.trim();
    state.site.avatarImage = elements.avatarImageInput.value.trim();
    state.site.footer = elements.footerInput.value.trim();
  }

  function attachTopLevelInputListeners() {
    [
      elements.siteNameInput,
      elements.brandInput,
      elements.taglineInput,
      elements.profileNameInput,
      elements.avatarTextInput,
      elements.avatarImageInput,
      elements.footerInput
    ].forEach(function (input) {
      input.addEventListener("input", function () {
        syncFromInputs();
        renderProfile();
      });
    });

    elements.avatarFileInput.addEventListener("change", function () {
      const selected = elements.avatarFileInput.files && elements.avatarFileInput.files[0];
      if (!selected) {
        return;
      }
      store.readFileAsDataUrl(selected).then(function (result) {
        state.site.avatarImage = result;
        elements.avatarImageInput.value = result;
        renderProfile();
      });
    });
  }

  function attachEditorActions() {
    elements.toggleEditor.addEventListener("click", function () {
      elements.editorPane.hidden = !elements.editorPane.hidden;
      if (!elements.editorPane.hidden) {
        renderEditor();
      }
    });

    elements.saveSite.addEventListener("click", function () {
      syncFromInputs();
      elements.saveSite.disabled = true;
      elements.saveSite.textContent = "保存中";
      saveRemoteProfile().then(function (profile) {
        state.site = mapProfileToSite(profile);
        localStorage.removeItem("sqh.site.data.v1");
        elements.editorPane.hidden = true;
        renderEditor();
        renderProfile();
        if (window.location.pathname === "/index.html") {
          window.location.replace("/");
        } else {
          window.location.reload();
        }
      }).catch(function (error) {
        state.site = store.saveSite(state.site);
        renderEditor();
        renderProfile();
        window.alert("服务器保存失败，已临时保存到当前浏览器：" + error.message);
      }).finally(function () {
        elements.saveSite.disabled = false;
        elements.saveSite.textContent = "保存主页";
      });
    });

    elements.resetSite.addEventListener("click", function () {
      localStorage.removeItem("sqh.site.data.v1");
      state.site = store.loadSite();
      renderEditor();
      renderProfile();
    });

    elements.addProfileLine.addEventListener("click", function () {
      state.site.profileLines.push("新增简介");
      renderEditor();
      renderProfile();
    });

    elements.addSkill.addEventListener("click", function () {
      state.site.skills.push(store.createSkill());
      renderEditor();
      renderProfile();
    });

    elements.addProject.addEventListener("click", function () {
      state.site.projects.push(store.createProject());
      renderEditor();
      renderProfile();
    });

    elements.addUpdate.addEventListener("click", function () {
      state.site.updates.push("新增动态");
      renderEditor();
      renderProfile();
    });

    elements.addContact.addEventListener("click", function () {
      state.site.contacts.push("新增联系项");
      renderEditor();
      renderProfile();
    });
  }

  attachTopLevelInputListeners();
  attachEditorActions();
  renderProfile();
  renderEditor();
  loadRemoteProfile();
})();
