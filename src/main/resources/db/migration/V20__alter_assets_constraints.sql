ALTER TABLE public.assets
DROP CONSTRAINT IF EXISTS child_assets_parent_asset_id_fkey;

ALTER TABLE public.assets
DROP CONSTRAINT IF EXISTS assets_parent_asset_id_fkey;