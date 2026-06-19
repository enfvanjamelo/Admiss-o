-- Migration: Create Admissions Table for Supabase Integration
-- Created on: 2026-06-17

-- Create table for admission records corresponding to the AdmissionRecord Kotlin class
CREATE TABLE IF NOT EXISTS admissions (
    id BIGINT PRIMARY KEY,
    timestamp BIGINT NOT NULL,
    
    -- 1. Dados do Paciente (Patient Info)
    nome TEXT DEFAULT '',
    idade TEXT DEFAULT '',
    sexo TEXT DEFAULT '',
    religiao TEXT DEFAULT '',
    estado_civil TEXT DEFAULT '',
    funcao_laboral TEXT DEFAULT '',
    proveniencia TEXT DEFAULT '',
    prontuario TEXT DEFAULT '',
    enfermaria TEXT DEFAULT '',
    leito TEXT DEFAULT '',
    tipo_cirurgia TEXT DEFAULT '',
    
    -- Vitals (Sinais Vitais)
    pressao_arterial TEXT DEFAULT '',
    frequencia_cardiaca TEXT DEFAULT '',
    frequencia_respiratoria TEXT DEFAULT '',
    temperatura TEXT DEFAULT '',
    saturacao_o2 TEXT DEFAULT '',
    
    -- 2. Sistemas Orgânicos & Dispositivos (Organ Systems & Devices)
    sistema_locomotor TEXT DEFAULT '',
    sistema_cardiovascular TEXT DEFAULT '',
    sistema_respiratorio TEXT DEFAULT '',
    sistema_neurologico TEXT DEFAULT '',
    sistema_nutricao TEXT DEFAULT '',
    sistema_urinario TEXT DEFAULT '',
    sistema_intestinal TEXT DEFAULT '',
    sistema_integridade_pele TEXT DEFAULT '',
    dispositivos TEXT DEFAULT '',
    
    -- Planimetria & Foto da Lesão (Wound Planimetry & Records)
    lesion_photo_uri TEXT,
    lesion_width_cm TEXT DEFAULT '',
    lesion_height_cm TEXT DEFAULT '',
    lesion_area_square_cm TEXT DEFAULT '',
    lesion_description_planimetria TEXT DEFAULT '',
    ai_interventions_result TEXT DEFAULT '',
    lesions_spreadsheet_json TEXT DEFAULT '',
    
    -- 3. Histórico Médico (Medical History)
    doencas_anteriores TEXT DEFAULT '',
    condicoes_anteriores TEXT DEFAULT '',
    cirurgias_anteriores TEXT DEFAULT '',
    alergias TEXT DEFAULT '',
    medicacoes_uso TEXT DEFAULT '',
    exames TEXT DEFAULT '',
    
    -- 4. Sensório, Mobilidade & Pele (Sensor, Mobility & Skin)
    acuidade_visual TEXT DEFAULT '',
    acuidade_auditiva TEXT DEFAULT '',
    locomocao_mobilidade TEXT DEFAULT '',
    integridade_pele TEXT DEFAULT '',
    apresenta_lesoes BOOLEAN DEFAULT FALSE,
    descricao_lesoes TEXT DEFAULT '',
    
    -- 5. Escala de Braden (Braden Scale for Pressure Injury Risk)
    braden_percepcao_sensorial INT DEFAULT 4,
    braden_umidade INT DEFAULT 4,
    braden_atividade INT DEFAULT 4,
    braden_mobilidade INT DEFAULT 4,
    braden_nutricao INT DEFAULT 4,
    braden_friccao_cisalhamento INT DEFAULT 3,
    
    -- 6. Escala de Fugulin (Fugulin Scale for Nursing Care Category)
    fugulin_estado_mental INT DEFAULT 1,
    fugulin_oxigenacao INT DEFAULT 1,
    fugulin_sinais_vitais INT DEFAULT 1,
    fugulin_motilidade INT DEFAULT 1,
    fugulin_locomocao INT DEFAULT 1,
    fugulin_cuidado_corporal INT DEFAULT 1,
    fugulin_eliminacao INT DEFAULT 1,
    fugulin_nutricao_hidratacao INT DEFAULT 1,
    fugulin_terapeutica INT DEFAULT 1,
    
    -- 7. Escala de Morse (Morse Fall Scale for Fall Risk)
    morse_historico_quedas INT DEFAULT 0,
    morse_diagnostico_secundario INT DEFAULT 0,
    morse_auxilio_locomocao INT DEFAULT 0,
    morse_terapia_ev INT DEFAULT 0,
    morse_marcha INT DEFAULT 0,
    morse_estado_mental INT DEFAULT 0,
    
    -- 8. Escala de Glasgow (Glasgow Coma Scale)
    glasgow_abertura_ocular INT DEFAULT 4,
    glasgow_resposta_verbal INT DEFAULT 5,
    glasgow_resposta_motora INT DEFAULT 6,
    
    -- 9. Escala de Dor (Pain Scale)
    dor_nivel INT DEFAULT 0,
    dor_localizacao TEXT DEFAULT '',
    dor_caracteristicas TEXT DEFAULT '',
    
    -- Metadata fields
    created_at TIMESTAMP WITH TIME ZONE DEFAULT TIMEZONE('utc'::text, NOW()) NOT NULL
);

-- Enable Row Level Security (RLS)
ALTER TABLE admissions ENABLE ROW LEVEL SECURITY;

-- Create policy to allow all anonymous reads/writes/updates/deletes for testing and prototyping
-- For production environments, these can be locked down to authenticated users.
CREATE POLICY "Allow public select all" ON admissions 
    FOR SELECT 
    USING (true);

CREATE POLICY "Allow public insert all" ON admissions 
    FOR INSERT 
    WITH CHECK (true);

CREATE POLICY "Allow public update all" ON admissions 
    FOR UPDATE 
    USING (true)
    WITH CHECK (true);

CREATE POLICY "Allow public delete all" ON admissions 
    FOR DELETE 
    USING (true);

-- Enable Realtime for the admissions table
ALTER PUBLICATION supabase_realtime ADD TABLE admissions;
