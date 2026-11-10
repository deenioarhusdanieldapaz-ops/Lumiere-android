package com.lumiere.app.ui.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

fun getCategoryIcon(categoryName: String?): ImageVector {
    if (categoryName.isNullOrBlank()) return Icons.Default.Star
    val name = categoryName.lowercase().trim()

    // 1. DICIONÁRIO MASSIVO (500+ palavras PT/EN)
    val matchedIcon = when {
        name.containsAny("cirurgi", "operaca", "recuper", "lesã", "lesao", "machucad", "ferid", "curativ", "tratament", "exame", "ultrassom", "raio x", "ressonânc", "ressonan") -> Icons.Default.LocalHospital
        name.containsAny("agua", "water", "hidrat", "beber", "beb", "sede", "líquid", "liquid", "suor", "urina") -> Icons.Default.WaterDrop
        name.containsAny("exerc", "gym", "ginás", "ginas", "corrid", "run", "desport", "fitness", "trein", "muscul", "força", "forca", "crossfit", "along", "natação", "natacao", "nata", "ciclism", "bike", "biciclet", "caminh", "passo", "step", "dança", "danca", "luta", "boxe", "marcial", "futebol", "basquet", "vôlei", "volei", "ténis", "tenis", "golfe") -> Icons.Default.FitnessCenter
        name.containsAny("medit", "yoga", "paz", "mind", "relax", "respir", "zen", "espiritual", "fé", "fe", "religi", "oração", "oracao", "reza", "igreja", "templo", "gratidão", "gratidao", "calma", "foco") -> Icons.Default.SelfImprovement
        name.containsAny("sono", "dormir", "sleep", "descans", "insônia", "insonia", "sesta", "cama", "travesseir", "sonh", "sonhos", "acordar") -> Icons.Default.Bedtime
        name.containsAny("estud", "curso", "aula", "escol", "school", "faculd", "univers", "study", "learn", "academ", "prova", "exame", "trabalh", "escolar", "matéri", "materia", "apostil", "resum", "vestibular", "concurso") -> Icons.Default.School
        name.containsAny("leitur", "livro", "ler", "read", "book", "artig", "bibliot", "romanc", "poesi", "históri", "historia", "biografi", "conto", "crônic", "cronic", "text", "pdf", "kindle", "e-book") -> Icons.Default.MenuBook
        name.containsAny("trabalh", "escrit", "reuni", "work", "job", "office", "empre", "deadlin", "apresent", "carreir", "currícul", "curricul", "entrevist", "chefe", "patrão", "patrao", "colega", "corporat", "empres", "negóci", "negoci", "business", "startup", "cliente", "client", "fornecedor") -> Icons.Default.Work
        name.containsAny("program", "códig", "codig", "code", "dev", "software", "app", "web", "site", "bug", "test", "qa", "html", "css", "javascript", "python", "java", "kotlin", "sql", "banco de dados", "servidor", "backend", "frontend", "api") -> Icons.Default.Code
        name.containsAny("financ", "dinheir", "money", "poupan", "econom", "banco", "invest", "moeda", "salári", "salari", "renda", "lucro", "gast", "receit", "orçament", "orcament", "cont", "conta", "impost", "tax", "bônus", "bonus", "dívid", "divida", "empréstim", "emprestim", "juros", "cripto", "crypto", "bitcoin", "ethereum", "blockchain", "nft") -> Icons.Default.AccountBalanceWallet
        name.containsAny("compra", "mercad", "shop", "buy", "supermerc", "loj", "store", "mall", "shopp", "carrinh", "sacol", "ofert", "promoç", "promoc", "descont", "cupom", "vend", "venda") -> Icons.Default.ShoppingCart
        name.containsAny("cartão", "cartao", "credit", "pagar", "pagament", "débit", "debit", "fatur", "boleto", "pix", "transferênc", "transferenc", "cheque") -> Icons.Default.CreditCard
        name.containsAny("casa", "lar", "home", "limpez", "arrum", "faxin", "organiz", "cozinh", "sala", "quart", "banheir", "jardim", "decor", "móve", "move", "eletro", "luz", "gás", "gas", "condomín", "condomin", "alugu", "mudanç", "mudanc", "reform", "pintur", "consert", "repar", "manutenç", "manutenc") -> Icons.Default.Home
        name.containsAny("famili", "family", "filh", "crianç", "crianc", "beb", "pai", "mãe", "mae", "irmã", "irma", "avó", "avo", "tio", "tia", "prim", "namorad", "marid", "espos", "casament", "anivers", "present", "fest", "encontr", "amig", "friend", "social", "date") -> Icons.Default.FamilyRestroom
        name.containsAny("pessoal", "self", "eu", "meu", "minh", "autocuidad", "autocuidado", "reflex", "diári", "diario", "journal", "hobby", "passatem", "tempo livr") -> Icons.Default.Person
        name.containsAny("comid", "restaur", "food", "cozinh", "receit", "diet", "nutric", "aliment", "jant", "almoç", "almoc", "café da manh", "lanch", "merend", "gourmet", "chef", "prat", "cardápi", "cardapi", "pizz", "hamburg", "fastfood", "doc", "sugar", "acucar", "sal", "temper", "carne", "peix", "veget", "frut", "salad") -> Icons.Default.Restaurant
        name.containsAny("café", "cafe", "coffee", "chá", "cha", "tea", "bebid", "suco", "sumo", "juice", "bar", "pub", "cervej", "vinh", "wine", "drink", "coquet") -> Icons.Default.LocalCafe
        name.containsAny("viag", "avião", "aviao", "flight", "férias", "ferias", "travel", "turism", "hotel", "pousad", "resort", "praia", "beach", "montanh", "camp", "trilh", "parqu", "park", "passe", "excurs", "mochil", "passaport", "passaport", "voo", "aeroport", "destin") -> Icons.Default.Flight
        name.containsAny("carro", "car", "conduz", "driving", "moto", "motocicl", "biciclet", "bike", "ônibus", "onibus", "bus", "metrô", "metro", "trem", "train", "táxi", "taxi", "uber", "combust", "gasolin", "pedági", "pedagi", "trânsit", "transit") -> Icons.Default.DirectionsCar
        name.containsAny("músic", "music", "cant", "instrument", "violã", "violao", "guitarr", "pian", "bater", "band", "banda", "show", "concert", "festival", "playlist", "spotify", "som", "áudio", "audio", "danç", "danc") -> Icons.Default.MusicNote
        name.containsAny("film", "séri", "serie", "cinem", "movie", "tv", "televis", "netflix", "stream", "document", "anime", "desenh", "cartoon", "youtub", "video", "vlog", "podcast") -> Icons.Default.Movie
        name.containsAny("jog", "game", "play", "video", "gamer", "console", "playstation", "xbox", "nintend", "steam", "pc gamer", "e-sport", "tabuleir", "xadrez", "cart", "baralh", "dado", "rpg") -> Icons.Default.SportsEsports
        name.containsAny("animal", "pet", "cão", "cao", "gato", "cat", "dog", "passar", "peix", "aquári", "aquari", "veterinár", "veterinar", "vacina", "racã", "racao", "passe", "adestr", "adestram") -> Icons.Default.Pets
        name.containsAny("art", "desenh", "pint", "criativ", "artesan", "escultur", "pincel", "tint", "aquarel", "ilustr", "design", "diagram", "maquet", "manual", "craft", "diy") -> Icons.Default.Brush
        name.containsAny("fot", "photo", "câmar", "camar", "camera", "registr", "memóri", "memori", "álbum", "album", "selfie", "portrait", "retrat", "fotograf", "vídeo", "video") -> Icons.Default.CameraAlt
        name.containsAny("tempo", "time", "hora", "relógi", "relogi", "cronometr", "timer", "pontual", "atras", "adiant", "duraç", "durac", "praz", "deadlin", "agend", "schedule") -> Icons.Default.AccessTime
        name.containsAny("data", "date", "calendár", "calendar", "agend", "event", "compromiss", "aniversár", "aniversar", "feriad", "férias", "ferias", "semana", "mês", "mes", "ano", "diári", "diari") -> Icons.Default.CalendarMonth
        name.containsAny("taref", "task", "afazer", "to-do", "checklist", "pendênc", "pendenc", "conclu", "finaliz", "feito", "realiz", "execut", "entreg") -> Icons.Default.CheckCircle
        name.containsAny("met", "goal", "objetiv", "objective", "alvo", "alcanç", "alcanc", "propósit", "proposit", "missã", "missao", "visã", "visao", "sonh", "sonho", "amb içã", "ambica") -> Icons.Default.Flag
        name.containsAny("hábit", "habit", "rotin", "routine", "diári", "diari", "repetiç", "repet", "ciclo", "constânc", "constanc", "disciplin", "consistênc", "consisten") -> Icons.Default.Autorenew
        name.containsAny("naturez", "nature", "plant", "jardim", "garden", "flor", "árvor", "arvor", "folh", "verdur", "hort", "agro", "ecolog", "ambient", "clima", "sustent", "reciclag") -> Icons.Default.Park
        name.containsAny("sol", "sun", "clima", "weather", "temperatur", "calor", "frio", "chuva", "rain", "neve", "snow", "vento", "tempest", "nublad", "céu", "ceu") -> Icons.Default.WbSunny
        name.containsAny("idei", "idea", "insight", "inspira", "criativ", "inova", "brainstorm", "pensament", "pensament", "reflex", "soluç", "soluc", "descobr", "inven") -> Icons.Default.Lightbulb
        else -> null
    }

    if (matchedIcon != null) return matchedIcon

    // 2. FALLBACK MÁGICO (Palavras não catalogadas)
    val fallbackIcons = listOf(
        Icons.Default.Star, Icons.Default.Bookmark, Icons.Default.Label,
        Icons.Default.PushPin, Icons.Default.Info, Icons.Default.Circle,
        Icons.Default.FavoriteBorder, Icons.Default.Lightbulb, Icons.Default.Bolt,
        Icons.Default.Spa
    )
    val index = Math.abs(name.hashCode()) % fallbackIcons.size
    return fallbackIcons[index]
}

private fun String.containsAny(vararg keywords: String): Boolean {
    return keywords.any { this.contains(it) }
}
