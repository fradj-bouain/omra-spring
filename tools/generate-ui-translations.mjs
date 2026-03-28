/**
 * Génère :
 * - src/main/resources/db/migration/V18__ui_translations.sql (si Flyway est activé)
 * - src/main/resources/i18n/ui-translations.seed.json (seeder au démarrage Spring)
 * Exécuter: node tools/generate-ui-translations.mjs
 */
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const root = path.join(__dirname, '..');
const out = path.join(root, 'src/main/resources/db/migration/V18__ui_translations.sql');
const seedJson = path.join(root, 'src/main/resources/i18n/ui-translations.seed.json');

function esc(s) {
  return String(s).replace(/'/g, "''");
}

const pairs = [];

function add(key, fr, ar) {
  pairs.push(['fr', key, fr], ['ar', key, ar]);
}

// ——— Common ———
add('common.save', 'Enregistrer', 'حفظ');
add('common.cancel', 'Annuler', 'إلغاء');
add('common.delete', 'Supprimer', 'حذف');
add('common.edit', 'Modifier', 'تعديل');
add('common.add', 'Ajouter', 'إضافة');
add('common.search', 'Rechercher', 'بحث');
add('common.loading', 'Chargement…', 'جاري التحميل…');
add('common.back', 'Retour', 'رجوع');
add('common.detail', 'Détail', 'التفاصيل');
add('common.create', 'Créer', 'إنشاء');
add('common.yes', 'Oui', 'نعم');
add('common.no', 'Non', 'لا');
add('common.close', 'Fermer', 'إغلاق');
add('common.optional', 'optionnel', 'اختياري');
add('common.required', 'Requis', 'مطلوب');
add('common.error', 'Erreur', 'خطأ');
add('common.success', 'Succès', 'تم بنجاح');
add('common.emDash', '—', '—');
add('common.openNew', 'Ouvrir dans un nouvel onglet', 'فتح في نافذة جديدة');
add('common.datePlaceholder', 'jj/mm/aaaa', 'يوم/شهر/سنة');
add('common.unknown', '?', '؟');

// Layout / nav
add('layout.logoAlt', 'Logo', 'الشعار');
add('layout.toggleMenu', 'Ouvrir le menu', 'فتح القائمة');
add('layout.expandNav', 'Développer la navigation', 'توسيع القائمة');
add('layout.collapseNav', 'Réduire la navigation', 'طي القائمة');
add('layout.userMenu', 'Menu utilisateur', 'قائمة المستخدم');
add('layout.settings', 'Paramètres', 'الإعدادات');
add('layout.logout', 'Déconnexion', 'تسجيل الخروج');
add('layout.footer', '© {{year}} Omra Dashboard', '© {{year}} لوحة عمرة');
add('nav.dashboard', 'Dashboard', 'لوحة التحكم');
add('nav.pilgrims', 'Pèlerins', 'الحجاج');
add('nav.groups', 'Groupes Omra', 'مجموعات العمرة');
add('nav.flights', 'Vols', 'الرحلات الجوية');
add('nav.hotels', 'Hôtels', 'الفنادق');
add('nav.documents', 'Documents', 'المستندات');
add('nav.payments', 'Paiements', 'المدفوعات');
add('nav.taskTemplates', 'Types de tâches', 'أنواع المهام');
add('nav.plannings', 'Plannings', 'البرامج');
add('nav.buses', 'Bus', 'الحافلات');
add('nav.notifications', 'Notifications', 'الإشعارات');
add('nav.users', 'Utilisateurs', 'المستخدمون');
add('nav.referral', 'Parrainage', 'الإحالة');
add('nav.agencies', 'Liste des agences', 'قائمة الوكالات');
add('nav.newAgency', 'Nouvelle agence', 'وكالة جديدة');

// Login
add('login.title', 'Connexion', 'تسجيل الدخول');
add('login.subtitle', 'Plateforme de gestion Omra', 'منصة إدارة العمرة');
add('login.email', 'Email', 'البريد الإلكتروني');
add('login.emailPlaceholder', 'vous@exemple.com', 'you@example.com');
add('login.emailRequired', 'Email requis', 'البريد الإلكتروني مطلوب');
add('login.emailInvalid', 'Email invalide', 'بريد إلكتروني غير صالح');
add('login.password', 'Mot de passe', 'كلمة المرور');
add('login.passwordRequired', 'Mot de passe requis', 'كلمة المرور مطلوبة');
add('login.submit', 'Se connecter', 'دخول');
add('login.langFr', 'Français', 'الفرنسية');
add('login.langAr', 'العربية', 'العربية');

// Settings
add('settings.title', 'Paramètres', 'الإعدادات');
add(
  'settings.pageIntro',
  'Personnalisez la langue, l’identité visuelle et les couleurs de votre espace.',
  'خصص اللغة والهوية البصرية وألوان مساحتك.'
);
add(
  'settings.paletteHint',
  'Choisissez une base puis affinez les codes couleur si besoin.',
  'اختر قاعدة ثم عدّل رموز الألوان عند الحاجة.'
);
add('settings.mediaSection', 'Logo & favicon', 'الشعار والأيقونة');
add('settings.colorsSection', 'Couleurs de l’interface', 'ألوان الواجهة');
add(
  'settings.colorsHint',
  'Chaque champ accepte un code HEX (#RRGGBB). Le carré ouvre le sélecteur système.',
  'كل حقل يقبل HEX (#RRGGBB). المربع يفتح منتقي الألوان.'
);
add(
  'settings.dropHint',
  'Cliquez pour choisir une image (PNG, JPEG, GIF, WebP).',
  'انقر لاختيار صورة (PNG، JPEG، GIF، WebP).'
);
add('settings.dropHintShort', 'PNG, ICO ou JPEG', 'PNG أو ICO أو JPEG');
add('settings.language', 'Langue de l’interface', 'لغة الواجهة');
add('settings.languageHint', 'Français par défaut. Les textes sont chargés depuis le serveur.', 'الفرنسية افتراضيًا. يتم تحميل النصوص من الخادم.');
add('settings.agencyProfile', 'Profil agence', 'ملف الوكالة');
add('settings.agencyProfileHint', 'Informations de votre organisation', 'معلومات مؤسستك');
add('settings.branding', 'Branding (thème)', 'الهوية (المظهر)');
add('settings.brandingSubtitle', 'Palette, couleurs HEX (#RRGGBB), logo et mode clair/sombre', 'لوحة ألوان، أكواد HEX (#RRGGBB)، الشعار والوضع الفاتح/الداكن');
add('settings.palettePreset', 'Palette prédéfinie', 'لوحة جاهزة');
add('settings.paletteCustom', 'Personnalisé', 'مخصص');
add('settings.palette.blueSaas', 'Blue SaaS (défaut)', 'Blue SaaS (افتراضي)');
add('settings.palette.greenIslamic', 'Green Islamic', 'أخضر إسلامي');
add('settings.palette.purpleModern', 'Purple Modern', 'بنفسجي عصري');
add('settings.palette.redBold', 'Red Bold', 'أحمر جريء');
add('settings.palette.darkPro', 'Dark Pro', 'داكن احترافي');
add('settings.palette.orangeFriendly', 'Orange Friendly', 'برتقالي ودود');
add('settings.themeMode', 'Mode thème', 'وضع المظهر');
add('settings.themeModeLight', 'Clair', 'فاتح');
add('settings.themeModeDark', 'Sombre', 'داكن');
add('settings.primaryColor', 'Couleur primaire', 'اللون الأساسي');
add('settings.secondaryColor', 'Couleur secondaire', 'اللون الثانوي');
add('settings.sidebarColor', 'Couleur barre latérale', 'لون الشريط الجانبي');
add('settings.menuColor', 'Couleur menu (lié à la barre)', 'لون القائمة');
add('settings.buttonColor', 'Couleur boutons', 'لون الأزرار');
add('settings.backgroundColor', 'Couleur de fond', 'لون الخلفية');
add('settings.cardColor', 'Couleur des cartes', 'لون البطاقات');
add('settings.textColor', 'Couleur du texte', 'لون النص');
add('settings.logoUrl', 'URL du logo (ou chemin après upload)', 'رابط الشعار (أو المسار بعد الرفع)');
add('settings.faviconUrl', 'URL du favicon (ou chemin après upload)', 'رابط الأيقونة (أو المسار بعد الرفع)');
add(
  'settings.storageHint',
  'Les fichiers sont enregistrés sur le serveur (dossier de stockage) ; l’URL complète est remplie automatiquement après upload.',
  'تُحفظ الملفات على الخادم (مجلد التخزين) ويُملأ الرابط تلقائياً بعد الرفع.'
);
add('settings.uploadLogo', 'Envoyer un logo', 'رفع شعار');
add('settings.uploadFavicon', 'Envoyer un favicon', 'رفع أيقونة');
add('settings.fileUploaded', 'Fichier enregistré sur le serveur', 'تم حفظ الملف على الخادم');
add('settings.fileUploadError', 'Échec de l’envoi du fichier', 'فشل رفع الملف');
add(
  'settings.fileUploadBadResponse',
  'Réponse invalide (URL manquante). Réessayez ou vérifiez la console réseau.',
  'استجابة غير صالحة (رابط مفقود). أعد المحاولة أو راجع أدوات الشبكة.'
);
add('settings.saveTheme', 'Enregistrer le thème', 'حفظ المظهر');
add('settings.previewHint', 'Aperçu : les couleurs sont appliquées après enregistrement.', 'معاينة: تُطبَّق الألوان بعد الحفظ.');
add('settings.themeSaved', 'Thème enregistré', 'تم حفظ المظهر');
add('settings.themeError', 'Erreur enregistrement thème', 'خطأ في حفظ المظهر');

// Dashboard
add('dashboard.title', 'Dashboard', 'لوحة التحكم');
add('dashboard.subtitle', 'Vue d’ensemble de votre activité Omra', 'نظرة عامة على نشاط العمرة');
add('dashboard.loadingStats', 'Chargement des statistiques…', 'جاري تحميل الإحصائيات…');
add('dashboard.totalPilgrims', 'Total pèlerins', 'إجمالي الحجاج');
add('dashboard.activeGroups', 'Groupes actifs', 'المجموعات النشطة');
add('dashboard.pendingVisas', 'Visas en attente', 'تأشيرات قيد الانتظار');
add('dashboard.paymentsReceived', 'Paiements reçus (MAD)', 'المدفوعات المستلمة (درهم)');
add('dashboard.totalRevenue', 'Revenus totaux', 'إجمالي الإيرادات');
add('dashboard.breakdown', 'Répartition', 'التوزيع');
add('dashboard.groupsActiveCount', 'Groupes actifs :', 'مجموعات نشطة:');
add('dashboard.visasPendingCount', 'Visas en attente :', 'تأشيرات قيد الانتظار:');
add('dashboard.kpiByGroup', 'KPIs par groupe', 'مؤشرات الأداء حسب المجموعة');
add('dashboard.col.group', 'Groupe', 'المجموعة');
add('dashboard.col.filled', 'Rempli', 'المعبأ');
add('dashboard.col.capacity', 'Capacité', 'السعة');
add('dashboard.col.paidMad', 'Payé (MAD)', 'المدفوع (درهم)');
add('dashboard.col.priceMad', 'Prix (MAD)', 'السعر (درهم)');
add('dashboard.col.status', 'Statut', 'الحالة');
add('dashboard.paymentsByPeriod', 'Paiements par période', 'المدفوعات حسب الفترة');
add('dashboard.visaDistribution', 'Répartition des visas', 'توزيع التأشيرات');
add('dashboard.noData', 'Aucune donnée', 'لا توجد بيانات');

// Generic list / table
add('list.count.one', '{{count}} élément', 'عنصر {{count}}');
add('list.count.many', '{{count}} éléments', '{{count}} عناصر');
add('list.count.groups.one', '{{count}} groupe', 'مجموعة {{count}}');
add('list.count.groups.many', '{{count}} groupes', '{{count}} مجموعات');
add('list.count.pilgrims.one', '{{count}} pèlerin', 'حاج {{count}}');
add('list.count.pilgrims.many', '{{count}} pèlerins', '{{count}} حجاج');
add('list.loading', 'Chargement…', 'جاري التحميل…');

// Groups
add('groups.listTitle', 'Groupes Omra', 'مجموعات العمرة');
add('groups.listSubtitle', 'Gérez vos groupes et départs', 'إدارة المجموعات والمغادرة');
add('groups.create', 'Créer groupe', 'إنشاء مجموعة');
add('groups.col.name', 'Nom groupe', 'اسم المجموعة');
add('groups.col.departure', 'Date départ', 'تاريخ المغادرة');
add('groups.col.return', 'Date retour', 'تاريخ العودة');
add('groups.col.capacity', 'Capacité', 'السعة');
add('groups.col.status', 'Statut', 'الحالة');
add('groups.loading', 'Chargement des groupes…', 'جاري تحميل المجموعات…');
add('groups.empty', 'Aucun groupe', 'لا توجد مجموعات');
add('groups.emptyDesc', 'Créez un groupe pour commencer.', 'أنشئ مجموعة للبدء.');
add('groups.form.new', 'Nouveau groupe Omra', 'مجموعة عمرة جديدة');
add('groups.form.edit', 'Modifier le groupe', 'تعديل المجموعة');
add('groups.form.name', 'Nom du groupe', 'اسم المجموعة');
add('groups.form.description', 'Description', 'الوصف');
add('groups.form.departure', 'Date de départ', 'تاريخ المغادرة');
add('groups.form.return', 'Date de retour', 'تاريخ العودة');
add('groups.form.maxCapacity', 'Capacité max', 'الحد الأقصى للسعة');
add('groups.form.price', 'Prix', 'السعر');
add('groups.form.planning', 'Planning', 'البرنامج');
add('groups.form.planningNone', '— Aucun —', '— لا يوجد —');
add('groups.form.status', 'Statut', 'الحالة');
add('groups.form.tripType', 'Type de voyage', 'نوع الرحلة');
add('groups.submit.create', 'Créer le groupe', 'إنشاء المجموعة');
add('groups.submit.save', 'Enregistrer', 'حفظ');
add('groups.detail.back', 'Retour aux groupes', 'العودة إلى المجموعات');
add('groups.detail.loading', 'Chargement du groupe…', 'جاري تحميل المجموعة…');
add('groups.detail.addTask', 'Ajouter une tâche', 'إضافة مهمة');
add('groups.detail.addPilgrim', 'Ajouter un pèlerin', 'إضافة حاج');
add('groups.detail.addPayment', 'Ajouter un paiement', 'إضافة دفعة');
add('groups.quick.addPayment', 'Ajouter un paiement', 'إضافة دفعة');
add('groups.tab.planning', 'Planning', 'البرنامج');
add('groups.tab.pilgrims', 'Pèlerins', 'الحجاج');
add('groups.tab.flights', 'Vols & Bus', 'الرحلات والحافلات');
add('groups.tab.hotels', 'Hôtels', 'الفنادق');
add('groups.tab.payments', 'Paiements & coûts', 'المدفوعات والتكاليف');
add('groups.tab.documents', 'Documents', 'المستندات');
add('groups.planning.cardTitle', 'Planning', 'البرنامج');
add('groups.planning.create', 'Créer un planning', 'إنشاء برنامج');
add('groups.planning.link', 'Associer un planning au groupe', 'ربط برنامج بالمجموعة');
add('groups.planning.select', 'Planning', 'البرنامج');
add('groups.planning.choose', '— Choisir —', '— اختر —');
add('groups.planning.linkBtn', 'Associer au groupe', 'ربط بالمجموعة');
add('groups.planning.linking', 'Association…', 'جاري الربط…');
add('groups.planning.banner', 'Planning :', 'البرنامج:');
add('groups.planning.treeTitle', 'Arborescence des tâches', 'شجرة المهام');
add('groups.planning.loadingProgram', 'Chargement du programme…', 'جاري تحميل البرنامج…');
add('groups.planning.empty', 'Aucun planning lié ou aucune tâche dans ce planning.', 'لا يوجد برنامج مرتبط أو لا توجد مهام.');
add('groups.planning.detailTitle', 'Détail', 'التفاصيل');
add('groups.pilgrims.search', 'Rechercher', 'بحث');
add('groups.pilgrims.searchPh', 'Nom, passeport, téléphone…', 'الاسم، جواز السفر، الهاتف…');
add('groups.pilgrims.add', 'Ajouter', 'إضافة');
add('groups.pilgrims.pilgrim', 'Pèlerin', 'الحاج');
add('groups.pilgrims.typeSearch', 'Tapez pour rechercher…', 'اكتب للبحث…');
add('groups.pilgrims.loadingP', 'Chargement…', 'جاري التحميل…');
add('groups.pilgrims.noneFound', 'Aucun pèlerin trouvé', 'لم يُعثر على حاج');
add('groups.pilgrims.addToGroup', 'Ajouter au groupe', 'إضافة للمجموعة');
add('groups.pilgrims.col.name', 'Nom', 'الاسم');
add('groups.pilgrims.col.passport', 'Passeport', 'جواز السفر');
add('groups.pilgrims.col.phone', 'Téléphone', 'الهاتف');
add('groups.pilgrims.col.visa', 'Visa', 'التأشيرة');
add('groups.pilgrims.tableEmpty', 'Aucun pèlerin ne correspond à la recherche.', 'لا يطابق البحث أي حاج.');
add('groups.companions.title', 'Accompagnateurs', 'المرافقون');
add('groups.companions.hint', 'Utilisateurs avec le rôle « Accompagnateur pèlerinage » affectés à ce groupe.', 'مستخدمون بدور « مرافق الحجاج » لهذه المجموعة.');
add('groups.companions.select', 'Accompagnateur(s)', 'المرافق(ون)');
add('groups.companions.placeholder', 'Choisir un ou plusieurs', 'اختر واحدًا أو أكثر');
add('groups.companions.save', 'Enregistrer les accompagnateurs', 'حفظ المرافقين');
add('groups.companions.saving', 'Enregistrement…', 'جاري الحفظ…');
add('groups.flights.title', 'Vols', 'الرحلات');
add('groups.flights.create', 'Créer un vol', 'إنشاء رحلة');
add('groups.flights.link', 'Lier un vol', 'ربط رحلة');
add('groups.flights.select', 'Vol', 'الرحلة');
add('groups.flights.linkBtn', 'Lier', 'ربط');
add('groups.flights.none', 'Aucun vol lié. Créez ou liez un vol.', 'لا توجد رحلة مرتبطة.');
add('groups.flights.seeAll', 'Voir tous les vols', 'عرض كل الرحلات');
add('groups.bus.title', 'Bus', 'الحافلات');
add('groups.bus.link', 'Lier un bus', 'ربط حافلة');
add('groups.bus.select', 'Bus', 'الحافلة');
add('groups.bus.none', 'Aucun bus lié.', 'لا توجد حافلة مرتبطة.');
add('groups.bus.seeAll', 'Voir les bus', 'عرض الحافلات');
add('groups.hotels.title', 'Hôtels', 'الفنادق');
add('groups.hotels.assign', 'Assigner un hôtel', 'تعيين فندق');
add('groups.hotels.hotel', 'Hôtel', 'الفندق');
add('groups.hotels.checkIn', 'Check-in', 'تسجيل الوصول');
add('groups.hotels.checkOut', 'Check-out', 'تسجيل المغادرة');
add('groups.hotels.city', 'Ville', 'المدينة');
add('groups.hotels.makkah', 'Makkah', 'مكة');
add('groups.hotels.madinah', 'Madinah', 'المدينة');
add('groups.hotels.roomType', 'Type de chambre', 'نوع الغرفة');
add('groups.hotels.assignBtn', 'Assigner', 'تعيين');
add('groups.hotels.none', 'Aucun hôtel assigné.', 'لا يوجد فندق معيّن.');
add('groups.hotels.seeAll', 'Voir les hôtels', 'عرض الفنادق');
add('groups.payments.title', 'Paiements', 'المدفوعات');
add('groups.payments.new', 'Nouveau paiement', 'دفعة جديدة');
add('groups.payments.none', 'Aucun paiement enregistré pour ce groupe.', 'لا توجد مدفوعات لهذه المجموعة.');
add('groups.costs.title', 'Coûts voyage', 'تكاليف الرحلة');
add('groups.costs.add', 'Ajouter un coût', 'إضافة تكلفة');
add('groups.costs.type', 'Type', 'النوع');
add('groups.costs.amount', 'Montant', 'المبلغ');
add('groups.costs.currency', 'Devise', 'العملة');
add('groups.costs.description', 'Description', 'الوصف');
add('groups.costs.addBtn', 'Ajouter', 'إضافة');
add('groups.costs.none', 'Aucun coût. Cliquez sur + pour en ajouter.', 'لا توجد تكاليف. انقر + للإضافة.');
add('groups.docs.title', 'Documents', 'المستندات');
add('groups.docs.manage', 'Gestion des documents', 'إدارة المستندات');
add('groups.docs.none', 'Aucun document lié à ce groupe (filtré sur les documents avec ce groupe).', 'لا توجد مستندات مرتبطة بهذه المجموعة.');
add('groups.hotels.assignedLine', 'Hôtel #{{id}} — {{in}} / {{out}}', 'فندق #{{id}} — {{in}} / {{out}}');
add('groups.bus.unnamed', 'Bus #{{id}}', 'حافلة #{{id}}');
add('groups.bus.seatsCount', '{{n}} places', '{{n}} مقعدًا');

// Pilgrims module
add('pilgrims.listTitle', 'Pèlerins', 'الحجاج');
add('pilgrims.listSubtitle', 'Gérez les pèlerins de vos groupes', 'إدارة حجاج مجموعاتك');
add('pilgrims.add', 'Ajouter pèlerin', 'إضافة حاج');
add('pilgrims.searchPh', 'Nom, passeport, nationalité…', 'الاسم، الجواز، الجنسية…');
add('pilgrims.searchAria', 'Rechercher un pèlerin', 'البحث عن حاج');
add('pilgrims.col.name', 'Nom', 'الاسم');
add('pilgrims.col.passport', 'Passeport', 'جواز السفر');
add('pilgrims.col.nationality', 'Nationalité', 'الجنسية');
add('pilgrims.col.phone', 'Téléphone', 'الهاتف');
add('pilgrims.col.visa', 'Visa', 'التأشيرة');
add('pilgrims.col.actions', 'Actions', 'إجراءات');
add('pilgrims.loading', 'Chargement des pèlerins…', 'جاري تحميل الحجاج…');
add('pilgrims.empty', 'Aucun pèlerin', 'لا يوجد حجاج');
add('pilgrims.emptyDesc', 'Ajoutez un pèlerin pour commencer.', 'أضف حاجًا للبدء.');
add('pilgrims.tooltip.detail', 'Détail', 'تفاصيل');
add('pilgrims.tooltip.edit', 'Modifier', 'تعديل');
add('pilgrims.tooltip.delete', 'Supprimer', 'حذف');
add('pilgrims.col.documents', 'Documents', 'المستندات');
add('pilgrims.linkAllDocuments', 'Tous les documents', 'كل المستندات');
add('pilgrims.documents.dialogTitle', 'Documents du pèlerin', 'مستندات الحاج');
add('pilgrims.documents.close', 'Fermer', 'إغلاق');
add('pilgrims.documents.openDialog', 'Voir les documents et pièces jointes', 'عرض المستندات والمرفقات');
add('pilgrims.documents.addButton', 'Ajouter un document', 'إضافة مستند');
add('pilgrims.documents.formSection', 'Nouveau document', 'مستند جديد');
add('pilgrims.documents.statusLabel', 'Statut du document', 'حالة المستند');
add('pilgrims.documents.formHint', 'Choisissez le type et le statut, puis sélectionnez un fichier pour l’envoyer.', 'اختر النوع والحالة ثم الملف للرفع.');
add('pilgrims.documents.download', 'Télécharger', 'تنزيل');
add('pilgrims.form.documentsHint', 'Après la création, vous serez redirigé pour joindre passeports, visas et autres pièces.', 'بعد الإنشاء ستُوجَّه لإرفاق الجوازات والتأشيرات وغيرها.');
add('pilgrims.form.uploadZoneTitle', 'Pièces jointes & documents', 'المرفقات والمستندات');
add(
  'pilgrims.form.uploadZoneDesc',
  'Après avoir enregistré le pèlerin, vous pourrez ajouter ici passeports, visas, billets et autres fichiers.',
  'بعد حفظ بيانات الحاج يمكنك إضافة الجوازات والتأشيرات والتذاكر وغيرها هنا.'
);
add('pilgrims.documents.history', 'Historique des documents', 'سجل المستندات');
add('pilgrims.documents.uploadSection', 'Ajouter un document', 'إضافة مستند');
add('pilgrims.documents.chooseType', 'Type', 'النوع');
add('pilgrims.documents.browse', 'Choisir un fichier', 'اختيار ملف');
add('pilgrims.documents.uploading', 'Envoi…', 'جاري الرفع…');
add('pilgrims.documents.empty', 'Aucun document pour ce pèlerin.', 'لا توجد مستندات لهذا الحاج.');
add('pilgrims.documents.open', 'Ouvrir', 'فتح');
add('pilgrims.documents.delete', 'Supprimer', 'حذف');
add('pilgrims.documents.deleteConfirm', 'Supprimer ce document ?', 'حذف هذا المستند؟');
add('pilgrims.documents.toggleExpand', 'Afficher ou masquer les documents', 'إظهار أو إخفاء المستندات');
add('pilgrims.documents.col.type', 'Type', 'النوع');
add('pilgrims.documents.col.status', 'Statut', 'الحالة');
add('pilgrims.documents.col.date', 'Date', 'التاريخ');
add('doc.type.PASSPORT', 'Passeport', 'جواز السفر');
add('doc.type.VISA', 'Visa', 'تأشيرة');
add('doc.type.FLIGHT_TICKET', 'Billet avion', 'تذكرة طيران');
add('doc.type.CONTRACT', 'Contrat', 'عقد');
add('doc.type.PROGRAM', 'Programme', 'برنامج');
add('doc.status.UPLOADED', 'Uploadé', 'مُرفوع');
add('doc.status.VERIFIED', 'Vérifié', 'مُتحقق');
add('doc.status.REJECTED', 'Refusé', 'مرفوض');
add('err.pilgrimDocumentsLoad', 'Erreur chargement documents', 'خطأ في تحميل المستندات');
add('pilgrims.documents.uploaded', 'Document ajouté', 'تمت إضافة المستند');
add('pilgrims.documents.deleted', 'Document supprimé', 'تم حذف المستند');
add('err.documentUpload', 'Échec de l\'upload', 'فشل الرفع');
add('err.documentSave', 'Erreur enregistrement document', 'خطأ في حفظ المستند');

// Visa / status labels (used in TS + template)
add('visa.pending', 'En attente', 'قيد الانتظار');
add('visa.submitted', 'Soumis', 'مُقدَّم');
add('visa.approved', 'Approuvé', 'مقبول');
add('visa.rejected', 'Refusé', 'مرفوض');
add('visa.unknown', '—', '—');

add('group.status.OPEN', 'Ouvert', 'مفتوح');
add('group.status.CLOSED', 'Fermé', 'مغلق');
add('group.status.FULL', 'Complet', 'مكتمل');
add('group.status.CONFIRMED', 'Confirmé', 'مؤكد');
add('group.status.COMPLETED', 'Terminé', 'منتهي');
add('err.groupsLoad', 'Erreur chargement groupes', 'خطأ في تحميل المجموعات');
add('payment.status.PENDING', 'En attente', 'قيد الانتظار');
add('payment.status.PAID', 'Payé', 'مدفوع');
add('payment.status.PARTIAL', 'Partiel', 'جزئي');
add('payment.status.REFUNDED', 'Remboursé', 'مسترد');

// Flights, hotels, buses, documents, payments (list/form) — short set
add('flights.listTitle', 'Vols', 'الرحلات');
add('flights.new', 'Nouveau vol', 'رحلة جديدة');
add('hotels.listTitle', 'Hôtels', 'الفنادق');
add('hotels.new', 'Nouvel hôtel', 'فندق جديد');
add('buses.listTitle', 'Bus', 'الحافلات');
add('buses.new', 'Nouveau bus', 'حافلة جديدة');
add('documents.listTitle', 'Documents', 'المستندات');
add('documents.new', 'Nouveau document', 'مستند جديد');
add('payments.listTitle', 'Paiements', 'المدفوعات');
add('payments.new', 'Nouveau paiement', 'دفعة جديدة');
add('plannings.listTitle', 'Plannings', 'البرامج');
add('plannings.new', 'Nouveau planning', 'برنامج جديد');
add('users.listTitle', 'Utilisateurs', 'المستخدمون');
add('users.new', 'Nouvel utilisateur', 'مستخدم جديد');
add('notifications.listTitle', 'Notifications', 'الإشعارات');
add('agencies.listTitle', 'Agences', 'الوكالات');
add('agencies.new', 'Nouvelle agence', 'وكالة جديدة');
add('taskTemplates.title', 'Types de tâches', 'أنواع المهام');
add('taskTemplates.newRoot', 'Nouvelle tâche racine', 'مهمة جذرية جديدة');

// Planning form
add('plannings.form.new', 'Nouveau planning', 'برنامج جديد');
add('plannings.form.edit', 'Modifier le planning', 'تعديل البرنامج');
add('plannings.form.name', 'Nom du planning', 'اسم البرنامج');
add('plannings.form.namePh', 'Ex. Programme 7 jours', 'مثال: برنامج 7 أيام');
add('plannings.form.description', 'Description', 'الوصف');
add('plannings.form.tasksTitle', 'Tâches du programme (ordre)', 'مهام البرنامج (الترتيب)');
add('plannings.form.mainTask', 'Tâche principale', 'المهمة الرئيسية');
add('plannings.form.choose', '— Choisir —', '— اختر —');
add('plannings.form.hint', 'Seules les tâches racines apparaissent ; les sous-tâches sont insérées automatiquement.', 'تظهر المهام الجذرية فقط؛ تُدرج المهام الفرعية تلقائيًا.');
add('plannings.form.subtasksAuto', '· {{n}} sous-tâche(s) ajoutées auto.', '· {{n}} مهمة فرعية تُضاف تلقائيًا.');
add('plannings.form.create', 'Créer', 'إنشاء');
add('plannings.saved', 'Planning modifié', 'تم تعديل البرنامج');
add('plannings.created', 'Planning créé', 'تم إنشاء البرنامج');
add('plannings.notFound', 'Planning introuvable', 'البرنامج غير موجود');
add('plannings.err', 'Erreur', 'خطأ');
add('plannings.form.add', 'Ajouter', 'إضافة');
add('plannings.form.empty', 'Aucune tâche. Choisissez une tâche principale ci-dessus et cliquez sur Ajouter.', 'لا توجد مهام. اختر مهمة رئيسية ثم «إضافة».');
add('plannings.form.saving', 'Enregistrement…', 'جاري الحفظ…');
add('plannings.form.save', 'Enregistrer', 'حفظ');
add('plannings.form.cancel', 'Annuler', 'إلغاء');

// Group stats (header cards)
add('stats.pilgrims', 'Pèlerins', 'الحجاج');
add('stats.payments', 'Paiements', 'المدفوعات');
add('stats.visas', 'Visas en attente', 'تأشيرات قيد الانتظار');
add('stats.documents', 'Documents', 'المستندات');

// Trip cost types
add('cost.FLIGHT', 'Vol', 'طيران');
add('cost.HOTEL', 'Hôtel', 'فندق');
add('cost.VISA', 'Visa', 'تأشيرة');
add('cost.TRANSPORT', 'Transport', 'نقل');
add('cost.MEALS', 'Repas', 'وجبات');
add('cost.OTHER', 'Autre', 'أخرى');

// Task template dialog / tree (minimal)
add('task.edit', 'Modifier', 'تعديل');
add('task.delete', 'Supprimer', 'حذف');
add('task.addChild', 'Ajouter une sous-tâche', 'إضافة مهمة فرعية');
add('task.duration', 'Durée', 'المدة');
add('task.minutes', 'min', 'د');
add('task.subtasks', 'Sous-tâches', 'مهام فرعية');
add('task.dragHint', 'Glisser pour réorganiser', 'اسحب لإعادة الترتيب');

// Task details panel
add('taskDetails.totalDuration', 'Durée totale (avec sous-tâches)', 'المدة الإجمالية (مع الفرعية)');
add('taskDetails.subtasksCount', '{{n}} sous-tâche(s)', '{{n}} مهمة فرعية');

// Group tabs labels (short)
add('groupTabs.planning', 'Planning', 'البرنامج');
add('groupTabs.pilgrims', 'Pèlerins', 'الحجاج');
add('groupTabs.flights', 'Vols', 'الرحلات');
add('groupTabs.flightsBus', 'Vols & Bus', 'الرحلات والحافلات');
add('groupTabs.hotels', 'Hôtels', 'الفنادق');
add('groupTabs.payments', 'Paiements', 'المدفوعات');
add('groupTabs.paymentsCosts', 'Paiements & coûts', 'المدفوعات والتكاليف');
add('groupTabs.documents', 'Documents', 'المستندات');

// Errors (group detail TS)
add('err.groupNotFound', 'Groupe introuvable', 'المجموعة غير موجودة');
add('err.groupLoad', 'Erreur lors du chargement des données du groupe', 'خطأ في تحميل بيانات المجموعة');
add('err.planningsLoad', 'Impossible de charger les plannings', 'تعذّر تحميل البرامج');
add('err.selectPlanning', 'Sélectionnez un planning', 'اختر برنامجًا');
add('err.planningLink', 'Erreur lors de l’association du planning', 'خطأ في ربط البرنامج');
add('err.pilgrimsLoad', 'Erreur chargement pèlerins', 'خطأ في تحميل الحجاج');
add('err.delete', 'Erreur suppression', 'خطأ في الحذف');
add('pilgrims.deleteConfirm', 'Supprimer ce pèlerin ?', 'حذف هذا الحاج؟');
add('pilgrims.deleted', 'Pèlerin supprimé', 'تم حذف الحاج');
add('login.error.badCredentials', 'Email ou mot de passe incorrect.', 'البريد أو كلمة المرور غير صحيحة.');
add('groups.detail.titleFallback', 'Détail groupe', 'تفاصيل المجموعة');
add('notif.planningLinked', 'Planning associé au groupe', 'تم ربط البرنامج بالمجموعة');
add('notif.hotelAssigned', 'Hôtel assigné', 'تم تعيين الفندق');
add('notif.flightLinked', 'Vol lié au groupe', 'تم ربط الرحلة بالمجموعة');
add('notif.busLinked', 'Bus lié au groupe', 'تم ربط الحافلة بالمجموعة');
add('groups.pilgrim.added', 'Pèlerin ajouté au groupe', 'تمت إضافة الحاج للمجموعة');
add('groups.pilgrim.removed', 'Pèlerin retiré', 'تمت إزالة الحاج');
add('groups.pilgrim.removeConfirm', 'Retirer ce pèlerin du groupe ?', 'إزالة هذا الحاج من المجموعة؟');
add('groups.hotel.selectError', 'Sélectionnez un hôtel', 'اختر فندقًا');
add('err.hotelsLoad', 'Impossible de charger les hôtels', 'تعذّر تحميل الفنادق');
add('err.flightsLoad', 'Impossible de charger les vols', 'تعذّر تحميل الرحلات');
add('err.busesLoad', 'Impossible de charger les bus', 'تعذّر تحميل الحافلات');
add('err.invalidAmount', 'Montant invalide', 'مبلغ غير صالح');
add('err.generic', 'Erreur', 'خطأ');
add('cost.added', 'Coût ajouté', 'تمت إضافة التكلفة');
add('groups.companions.savedNotif', 'Accompagnateurs mis à jour', 'تم تحديث المرافقين');
add('err.missingGroupId', 'ID groupe manquant', 'معرّف المجموعة مفقود');
add('err.pilgrimCatalogLoad', 'Impossible de charger les pèlerins', 'تعذّر تحميل قائمة الحجاج');

let sql = `-- UI translations (FR + AR), generated by tools/generate-ui-translations.mjs
CREATE TABLE IF NOT EXISTS ui_translations (
    id BIGSERIAL PRIMARY KEY,
    locale VARCHAR(10) NOT NULL,
    msg_key VARCHAR(255) NOT NULL,
    msg_value TEXT NOT NULL,
    CONSTRAINT uk_ui_translations_locale_key UNIQUE (locale, msg_key)
);
CREATE INDEX IF NOT EXISTS idx_ui_translations_locale ON ui_translations(locale);
`;

const frVals = pairs.filter((p) => p[0] === 'fr');
const chunk = (arr, size) => {
  const out = [];
  for (let i = 0; i < arr.length; i += size) out.push(arr.slice(i, i + size));
  return out;
};

for (const batch of chunk(pairs, 80)) {
  const lines = batch.map(([loc, k, v]) => `('${esc(loc)}', '${esc(k)}', '${esc(v)}')`);
  sql += `INSERT INTO ui_translations (locale, msg_key, msg_value) VALUES\n${lines.join(',\n')}\nON CONFLICT (locale, msg_key) DO UPDATE SET msg_value = EXCLUDED.msg_value;\n\n`;
}

fs.mkdirSync(path.dirname(seedJson), { recursive: true });
const seedObj = {
  generatedBy: 'tools/generate-ui-translations.mjs',
  entries: pairs.map(([locale, k, v]) => ({ locale, msgKey: k, msgValue: v })),
};
fs.writeFileSync(seedJson, JSON.stringify(seedObj), 'utf8');
fs.writeFileSync(out, sql, 'utf8');
console.log('Wrote', out, 'rows:', pairs.length / 2, 'keys');
console.log('Wrote', seedJson, 'entries:', seedObj.entries.length);
